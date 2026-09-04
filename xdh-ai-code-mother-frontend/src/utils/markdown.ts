import hljs from 'highlight.js/lib/core'
import css from 'highlight.js/lib/languages/css'
import javascript from 'highlight.js/lib/languages/javascript'
import xml from 'highlight.js/lib/languages/xml'
import MarkdownIt from 'markdown-it'

import { unwrapChatPayload } from '@/utils/chatPayload'

hljs.registerLanguage('xml', xml)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('vue', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)

const SUPPORTED_LANGS = ['html', 'xml', 'vue', 'css', 'javascript', 'js'] as const

const escapeHtml = (value: string) =>
  value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')

const highlightCode = (code: string, language?: string) => {
  const normalizedLang = language?.toLowerCase().trim() || ''

  if (normalizedLang && hljs.getLanguage(normalizedLang)) {
    try {
      return hljs.highlight(code, {
        language: normalizedLang,
        ignoreIllegals: true,
      }).value
    } catch {
      // fall through
    }
  }

  try {
    return hljs.highlightAuto(code, [...SUPPORTED_LANGS]).value
  } catch {
    return escapeHtml(code)
  }
}

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  highlight(code, language) {
    const highlighted = highlightCode(code, language)
    const langClass = language ? ` language-${escapeHtml(language)}` : ''
    return `<pre class="hljs${langClass}"><code>${highlighted}</code></pre>`
  },
})

type FenceMatch = {
  marker: string
  suffix: string
}

const parseFenceLine = (line: string): FenceMatch | null => {
  const match = line.match(/^\s{0,3}(`{3,})([^`]*)$/)

  if (!match) {
    return null
  }

  const marker = match[1]
  const suffix = match[2]

  if (!marker || suffix === undefined) {
    return null
  }

  return {
    marker,
    suffix: suffix.trim(),
  }
}

const normalizeCodeBlocks = (content: string) => {
  const normalizedContent = content.replaceAll('\r\n', '\n')
  const lines = normalizedContent.split('\n')
  const normalizedLines: string[] = []
  let openFence: string | null = null

  for (const line of lines) {
    const fence = parseFenceLine(line)

    if (!openFence) {
      normalizedLines.push(line)

      if (fence) {
        openFence = fence.marker
      }

      continue
    }

    if (fence && !fence.suffix && fence.marker.length >= openFence.length) {
      normalizedLines.push(line)
      openFence = null
      continue
    }

    const startsNextToolCall = line.trimStart().startsWith('[工具调用]')
    const startsNextFence = Boolean(fence && fence.suffix)

    if (startsNextToolCall || startsNextFence) {
      normalizedLines.push(openFence)
      openFence = null
      normalizedLines.push(line)

      if (fence) {
        openFence = fence.marker
      }

      continue
    }

    normalizedLines.push(line)
  }

  if (openFence) {
    normalizedLines.push(openFence)
  }

  return normalizedLines.join('\n')
}

const normalizeChatContent = (content: string) => {
  const normalized = unwrapChatPayload(content)
  return normalizeCodeBlocks(normalized)
}

export const renderMarkdown = (content?: string | null) => {
  if (!content) {
    return ''
  }

  return markdown.render(normalizeChatContent(content))
}
