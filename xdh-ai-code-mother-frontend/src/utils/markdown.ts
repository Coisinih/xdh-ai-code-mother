import hljs from 'highlight.js/lib/core'
import css from 'highlight.js/lib/languages/css'
import javascript from 'highlight.js/lib/languages/javascript'
import xml from 'highlight.js/lib/languages/xml'
import MarkdownIt from 'markdown-it'

hljs.registerLanguage('xml', xml)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)

const SUPPORTED_LANGS = ['html', 'xml', 'css', 'javascript', 'js'] as const

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

export const renderMarkdown = (content?: string | null) => {
  if (!content) {
    return ''
  }

  return markdown.render(content)
}
