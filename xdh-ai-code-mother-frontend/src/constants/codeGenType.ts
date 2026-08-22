export const CodeGenTypeEnum = {
  HTML: 'html',
  MULTI_FILE: 'multi_file',
  VUE_PROJECT: 'vue_project',
} as const

export type CodeGenType = (typeof CodeGenTypeEnum)[keyof typeof CodeGenTypeEnum]

const CODE_GEN_TYPE_LABELS: Record<CodeGenType, string> = {
  [CodeGenTypeEnum.HTML]: '原生 HTML 模式',
  [CodeGenTypeEnum.MULTI_FILE]: '原生多文件模式',
  [CodeGenTypeEnum.VUE_PROJECT]: 'Vue 工程项目',
}

export const codeGenTypeOptions: { label: string; value: CodeGenType }[] = (
  Object.entries(CODE_GEN_TYPE_LABELS) as [CodeGenType, string][]
).map(([value, label]) => ({ value, label }))

export const getCodeGenTypeText = (value?: string) => {
  if (!value) {
    return '-'
  }

  return CODE_GEN_TYPE_LABELS[value as CodeGenType] ?? value
}
