import axios from 'axios'

export const engineApi = {
  async generateHtml(tplData: object, printData: object[]): Promise<Blob> {
    const res = await axios.post('/api/engine/generateHtml', { tplData, printData }, {
      responseType: 'blob',
      withCredentials: true,
    })
    const blob: Blob = res.data
    if (blob.type && blob.type.includes('application/json')) {
      const text = await blob.text()
      let message = 'HTML 生成失败'
      try {
        const json = JSON.parse(text)
        message = json.message || message
      } catch { /* ignore */ }
      throw new Error(message)
    }
    return blob
  },

  async generatePdf(tplData: object, printData: object[]): Promise<Blob> {
    const res = await axios.post('/api/engine/generatePdf', { tplData, printData }, {
      responseType: 'blob',
      withCredentials: true,
    })
    const blob: Blob = res.data
    if (blob.type && blob.type.includes('application/json')) {
      const text = await blob.text()
      let message = 'PDF 生成失败'
      try {
        const json = JSON.parse(text)
        message = json.message || message
      } catch { /* ignore */ }
      throw new Error(message)
    }
    return blob
  }
}
