import hljs from 'highlight.js/lib/core'
import css from 'highlight.js/lib/languages/css'
import javascript from 'highlight.js/lib/languages/javascript'
import xml from 'highlight.js/lib/languages/xml'
import MarkdownIt from 'markdown-it'

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

const normalizeCodeBlocks = (content: string) => {
  const fence = '```'
  let cursor = 0
  let output = ''

  while (cursor < content.length) {
    const start = content.indexOf(fence, cursor)
    if (start < 0) {
      output += content.slice(cursor)
      break
    }

    output += content.slice(cursor, start)

    const openingLineEnd = content.indexOf('\n', start + fence.length)
    if (openingLineEnd < 0) {
      output += content.slice(start)
      break
    }

    const language = content
      .slice(start + fence.length, openingLineEnd)
      .trim()
      .replace(/\r$/, '')
    const codeStart = openingLineEnd + 1
    const end = content.indexOf(fence, codeStart)

    if (end < 0) {
      // Keep an incomplete block intact while the stream is still arriving.
      output += content.slice(start)
      break
    }

    const code = content.slice(codeStart, end)
    output += `${fence}${language}\n${code}${fence}`
    cursor = end + fence.length
  }

  return output
}

const normalizeChatContent = (content: string) => {
  let normalized = content

  // Handle a complete SSE envelope that reached the renderer unchanged.
  try {
    const parsed = JSON.parse(normalized) as { d?: unknown }
    if (typeof parsed.d === 'string') {
      normalized = parsed.d
    }
  } catch {
    // Normal assistant text is not JSON.
  }

  return normalizeCodeBlocks(normalized)
}

export const renderMarkdown = (content?: string | null) => {
  if (!content) {
    return ''
  }

  return markdown.render(normalizeChatContent(content))
}
