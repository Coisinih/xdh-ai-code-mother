import { Modal } from 'ant-design-vue'

type ConfirmDangerActionOptions = {
  title: string
  content?: string
  okText?: string
  cancelText?: string
  onOk: () => Promise<void> | void
}

export const confirmDangerAction = ({
  title,
  content = '删除后无法恢复。',
  okText = '删除',
  cancelText = '取消',
  onOk,
}: ConfirmDangerActionOptions) => {
  Modal.confirm({
    title,
    content,
    okText,
    okType: 'danger',
    cancelText,
    async onOk() {
      await onOk()
    },
  })
}
