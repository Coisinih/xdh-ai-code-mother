const isWhitespace = (char?: string) => {
  return char === ' ' || char === '\n' || char === '\r' || char === '\t'
}

const decodeJsonStringToken = (source: string, startIndex: number) => {
  let cursor = startIndex
  let value = ''

  while (cursor < source.length) {
    const current = source[cursor]

    if (current === '"') {
      return {
        value,
        endIndex: cursor + 1,
        terminated: true,
      }
    }

    if (current === '\\') {
      const next = source[cursor + 1]
      if (next === undefined) {
        return {
          value: `${value}\\`,
          endIndex: cursor + 1,
          terminated: false,
        }
      }

      if (next === 'u') {
        const hex = source.slice(cursor + 2, cursor + 6)
        if (/^[0-9a-fA-F]{4}$/.test(hex)) {
          value += String.fromCharCode(Number.parseInt(hex, 16))
          cursor += 6
          continue
        }
      }

      switch (next) {
        case '"':
          value += '"'
          break
        case '\\':
          value += '\\'
          break
        case '/':
          value += '/'
          break
        case 'b':
          value += '\b'
          break
        case 'f':
          value += '\f'
          break
        case 'n':
          value += '\n'
          break
        case 'r':
          value += '\r'
          break
        case 't':
          value += '\t'
          break
        default:
          value += next
          break
      }

      cursor += 2
      continue
    }

    value += current
    cursor += 1
  }

  return {
    value,
    endIndex: cursor,
    terminated: false,
  }
}

const collectJsonStringPropertyValues = (source: string, propertyName: string) => {
  const propertyToken = `"${propertyName}"`
  const values: string[] = []
  let cursor = 0

  while (cursor < source.length) {
    const propertyIndex = source.indexOf(propertyToken, cursor)
    if (propertyIndex < 0) {
      break
    }

    let valueStart = propertyIndex + propertyToken.length

    while (isWhitespace(source[valueStart])) {
      valueStart += 1
    }

    if (source[valueStart] !== ':') {
      cursor = propertyIndex + propertyToken.length
      continue
    }

    valueStart += 1
    while (isWhitespace(source[valueStart])) {
      valueStart += 1
    }

    if (source[valueStart] !== '"') {
      cursor = valueStart
      continue
    }

    const parsed = decodeJsonStringToken(source, valueStart + 1)
    values.push(parsed.value)
    cursor = parsed.endIndex

    if (!parsed.terminated) {
      break
    }
  }

  return values
}

const looksLikeWrappedPayload = (payload: string) => {
  const trimmed = payload.trim()

  if (!trimmed.includes('"d"')) {
    return false
  }

  const looksLikeEnvelope =
    trimmed.startsWith('{') ||
    trimmed.startsWith('data:') ||
    trimmed.startsWith('"d"') ||
    trimmed.includes('{"d":')

  if (!looksLikeEnvelope) {
    return false
  }

  return trimmed.includes('\\n') || trimmed.includes('\\"') || trimmed.includes('\\u')
}

export const unwrapChatPayload = (payload: string) => {
  if (!payload) {
    return payload
  }

  try {
    const parsed = JSON.parse(payload) as { d?: unknown }
    if (typeof parsed.d === 'string') {
      return parsed.d
    }
  } catch {
    // Fall back to a tolerant extractor for partially wrapped stream chunks.
  }

  if (!looksLikeWrappedPayload(payload)) {
    return payload
  }

  const fragments = collectJsonStringPropertyValues(payload, 'd')
  if (fragments.length > 0) {
    return fragments.join('')
  }

  return payload
}
