import { API_BASE_URL } from '@/config/runtime'
import { type AppIdentifier } from '@/utils/app'

type StreamHandlers = {
  onChunk: (chunk: string) => void
  onDone?: () => void
}

type ParsedSsePayload = {
  data: string
  event?: string
}

const unwrapChunkPayload = (payload: string) => {
  try {
    const parsed = JSON.parse(payload) as { d?: string }
    if (typeof parsed.d === 'string') {
      return parsed.d
    }
  } catch {
    // Fall back to raw payload when the backend returns plain text.
  }

  return payload
}

const parseSseBlock = (block: string): ParsedSsePayload => {
  const normalizedBlock = block.trim()
  if (!normalizedBlock) {
    return { data: '' }
  }

  const lines = normalizedBlock.split(/\r?\n/)
  const dataLines = lines
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).trimStart())
  const eventLine = lines.find((line) => line.startsWith('event:'))

  return {
    data: dataLines.length > 0 ? unwrapChunkPayload(dataLines.join('\n')) : normalizedBlock,
    event: eventLine?.slice(6).trim(),
  }
}

export const streamChatToGenCode = async (
  appId: AppIdentifier,
  message: string,
  handlers: StreamHandlers,
  signal?: AbortSignal
) => {
  const url = new URL(`${API_BASE_URL}/app/chat/gen/code`)
  url.searchParams.set('appId', String(appId))
  url.searchParams.set('message', message)

  const response = await fetch(url.toString(), {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'text/event-stream',
    },
    signal,
  })

  if (!response.ok || !response.body) {
    throw new Error(`生成失败：${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    buffer += decoder.decode(value || new Uint8Array(), { stream: !done })

    const blocks = buffer.split(/\r?\n\r?\n/)
    buffer = blocks.pop() ?? ''

    for (const block of blocks) {
      const payload = parseSseBlock(block)

      if (payload.event === 'done') {
        handlers.onDone?.()
        continue
      }

      if (!payload.data || payload.data === '[DONE]') {
        continue
      }

      handlers.onChunk(payload.data)
    }

    if (done) {
      if (buffer.trim()) {
        const finalPayload = parseSseBlock(buffer)
        if (finalPayload.event === 'done') {
          handlers.onDone?.()
        } else if (finalPayload.data && finalPayload.data !== '[DONE]') {
          handlers.onChunk(finalPayload.data)
        }
      }
      break
    }
  }
}
