/** Visual editor bridge for a same-origin preview iframe. */
export interface ElementInfo {
  tagName: string
  id: string
  className: string
  textContent: string
  selector: string
  pagePath: string
  rect: {
    top: number
    left: number
    width: number
    height: number
  }
}

export interface VisualEditorOptions {
  onElementSelected?: (elementInfo: ElementInfo) => void
  onElementHover?: (elementInfo: ElementInfo) => void
}

type EditorMessage = {
  source?: string
  type?: string
  editMode?: boolean
  data?: {
    elementInfo?: ElementInfo
  }
}

const SCRIPT_ID = 'visual-editor-runtime'
const STYLE_ID = 'visual-editor-styles'
const TIP_ID = 'visual-editor-tip'
const EDITOR_SOURCE = 'app-visual-editor'

export class VisualEditor {
  private iframe: HTMLIFrameElement | null = null
  private iframeDocument: Document | null = null
  private isEditMode = false
  private options: VisualEditorOptions
  private retryTimer: number | null = null

  constructor(options: VisualEditorOptions = {}) {
    this.options = options
  }

  init(iframe: HTMLIFrameElement) {
    if (this.iframe !== iframe) {
      this.stopRetrying()
    }
    this.iframe = iframe
    this.bindDocument()
  }

  enableEditMode() {
    this.isEditMode = true
    this.ensureRuntime()
  }

  disableEditMode() {
    this.isEditMode = false
    this.stopRetrying()
    this.postToIframe({ type: 'TOGGLE_EDIT_MODE', editMode: false })
    this.postToIframe({ type: 'CLEAR_ALL_EFFECTS' })
    this.removeInjectedEffects()
  }

  toggleEditMode() {
    if (this.isEditMode) {
      this.disableEditMode()
    } else {
      this.enableEditMode()
    }
    return this.isEditMode
  }

  syncState() {
    if (this.isEditMode) {
      this.ensureRuntime()
    } else {
      this.postToIframe({ type: 'CLEAR_ALL_EFFECTS' })
      this.removeInjectedEffects()
    }
  }

  clearSelection() {
    this.postToIframe({ type: 'CLEAR_SELECTION' })
    this.iframeDocument?.querySelectorAll('.visual-editor-selected').forEach((element) => {
      element.classList.remove('visual-editor-selected')
    })
  }

  onIframeLoad() {
    this.stopRetrying()
    this.iframeDocument = null
    this.bindDocument()

    if (this.isEditMode) {
      this.ensureRuntime()
    } else {
      this.postToIframe({ type: 'CLEAR_ALL_EFFECTS' })
    }
  }

  handleIframeMessage = (event: MessageEvent<EditorMessage>) => {
    const iframeWindow = this.iframe?.contentWindow
    if (!iframeWindow || event.source !== iframeWindow) {
      return
    }

    const message = event.data || {}
    if (message.source !== EDITOR_SOURCE) {
      return
    }

    const elementInfo = message.data?.elementInfo
    if (message.type === 'ELEMENT_SELECTED' && elementInfo) {
      this.options.onElementSelected?.(elementInfo)
    }

    if (message.type === 'ELEMENT_HOVER' && elementInfo) {
      this.options.onElementHover?.(elementInfo)
    }
  }

  destroy() {
    this.disableEditMode()
    this.iframe = null
    this.iframeDocument = null
  }

  private bindDocument() {
    try {
      this.iframeDocument = this.iframe?.contentDocument || null
    } catch {
      this.iframeDocument = null
    }
  }

  private ensureRuntime() {
    if (!this.iframe) {
      return
    }

    this.bindDocument()
    const document = this.iframeDocument
    if (!document?.head || !document.body) {
      this.scheduleRetry()
      return
    }

    let script = document.getElementById(SCRIPT_ID) as HTMLScriptElement | null
    if (!script) {
      script = document.createElement('script')
      script.id = SCRIPT_ID
      script.textContent = this.getRuntimeScript()
      document.head.appendChild(script)
    }

    this.injectStyles(document)
    this.postToIframe({ type: 'TOGGLE_EDIT_MODE', editMode: true })
    this.removeRetryTimer()
  }

  private scheduleRetry() {
    if (this.retryTimer !== null) {
      return
    }

    this.retryTimer = window.setTimeout(() => {
      this.retryTimer = null
      if (this.isEditMode) {
        this.ensureRuntime()
      }
    }, 100)
  }

  private removeRetryTimer() {
    if (this.retryTimer !== null) {
      window.clearTimeout(this.retryTimer)
      this.retryTimer = null
    }
  }

  private stopRetrying() {
    this.removeRetryTimer()
  }

  private postToIframe(message: Record<string, unknown>) {
    this.iframe?.contentWindow?.postMessage(message, '*')
  }

  private injectStyles(document: Document) {
    if (document.getElementById(STYLE_ID)) {
      return
    }

    const style = document.createElement('style')
    style.id = STYLE_ID
    style.textContent = `
      .visual-editor-hover {
        outline: 2px dashed #1677ff !important;
        outline-offset: 2px !important;
        cursor: crosshair !important;
      }
      .visual-editor-selected {
        outline: 3px solid #0958d9 !important;
        outline-offset: 2px !important;
        cursor: crosshair !important;
      }
    `
    document.head.appendChild(style)
  }

  private removeInjectedEffects() {
    const document = this.iframeDocument
    if (!document) {
      return
    }

    document.querySelectorAll('.visual-editor-hover, .visual-editor-selected').forEach((element) => {
      element.classList.remove('visual-editor-hover', 'visual-editor-selected')
    })
    document.getElementById(TIP_ID)?.remove()
  }

  private getRuntimeScript() {
    return `
      (() => {
        const SOURCE = '${EDITOR_SOURCE}';
        const HOVER_CLASS = 'visual-editor-hover';
        const SELECTED_CLASS = 'visual-editor-selected';
        const TIP_ID = '${TIP_ID}';
        let editMode = false;
        let hoverElement = null;
        let selectedElement = null;
        let listenersReady = false;

        const isIgnored = (element) => {
          if (!(element instanceof HTMLElement)) return true;
          if (element === document.body || element === document.documentElement) return true;
          return ['SCRIPT', 'STYLE', 'LINK', 'META', 'HEAD', 'TITLE'].includes(element.tagName);
        };

        const getTarget = (event) => {
          const element = event.target instanceof HTMLElement ? event.target : null;
          return element && !isIgnored(element) && !element.closest('#' + TIP_ID) ? element : null;
        };

        const clearHover = () => {
          hoverElement?.classList.remove(HOVER_CLASS);
          hoverElement = null;
        };

        const clearSelected = () => {
          document.querySelectorAll('.' + SELECTED_CLASS).forEach((element) => {
            element.classList.remove(SELECTED_CLASS);
          });
          selectedElement = null;
        };

        const selectorFor = (element) => {
          const path = [];
          let current = element;
          while (current && current !== document.body) {
            let selector = current.tagName.toLowerCase();
            if (current.id) {
              path.unshift(selector + '#' + current.id);
              break;
            }
            const classes = typeof current.className === 'string'
              ? current.className.split(/\\s+/).filter((name) => name && !name.startsWith('visual-editor-'))
              : [];
            if (classes.length) selector += '.' + classes.join('.');
            const parent = current.parentElement;
            if (parent) {
              const sameTag = Array.from(parent.children).filter((node) => node.tagName === current.tagName);
              if (sameTag.length > 1) selector += ':nth-of-type(' + (sameTag.indexOf(current) + 1) + ')';
            }
            path.unshift(selector);
            current = current.parentElement;
          }
          return path.join(' > ');
        };

        const infoFor = (element) => {
          const rect = element.getBoundingClientRect();
          return {
            tagName: element.tagName,
            id: element.id || '',
            className: typeof element.className === 'string' ? element.className : '',
            textContent: element.textContent?.trim().slice(0, 100) || '',
            selector: selectorFor(element),
            pagePath: window.location.search + window.location.hash,
            rect: { top: rect.top, left: rect.left, width: rect.width, height: rect.height },
          };
        };

        const send = (type, element) => {
          window.parent.postMessage({ source: SOURCE, type, data: { elementInfo: infoFor(element) } }, '*');
        };

        const onMouseOver = (event) => {
          if (!editMode) return;
          const target = getTarget(event);
          if (!target || target === hoverElement || target === selectedElement) return;
          clearHover();
          target.classList.add(HOVER_CLASS);
          hoverElement = target;
          send('ELEMENT_HOVER', target);
        };

        const onMouseOut = (event) => {
          if (!editMode || !hoverElement) return;
          const related = event.relatedTarget;
          if (related instanceof Node && hoverElement.contains(related)) return;
          clearHover();
        };

        const onClick = (event) => {
          if (!editMode) return;
          const target = getTarget(event);
          if (!target) return;
          event.preventDefault();
          event.stopPropagation();
          event.stopImmediatePropagation();
          clearHover();
          clearSelected();
          target.classList.add(SELECTED_CLASS);
          selectedElement = target;
          send('ELEMENT_SELECTED', target);
        };

        const setup = () => {
          if (listenersReady || !document.body) return;
          document.addEventListener('mouseover', onMouseOver, true);
          document.addEventListener('mouseout', onMouseOut, true);
          document.addEventListener('click', onClick, true);
          listenersReady = true;
        };

        window.addEventListener('message', (event) => {
          if (event.source !== window.parent) return;
          const message = event.data || {};
          if (message.type === 'TOGGLE_EDIT_MODE') {
            editMode = Boolean(message.editMode);
            setup();
            if (!editMode) {
              clearHover();
              clearSelected();
              document.getElementById(TIP_ID)?.remove();
            }
          }
          if (message.type === 'CLEAR_SELECTION') clearSelected();
          if (message.type === 'CLEAR_ALL_EFFECTS') {
            editMode = false;
            clearHover();
            clearSelected();
            document.getElementById(TIP_ID)?.remove();
          }
        });

        setup();
      })();
    `
  }
}
