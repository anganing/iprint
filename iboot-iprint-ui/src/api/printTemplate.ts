import http from './index'

export interface PrintTemplateItem {
  id: number
  code: string
  name: string
  templateData: string
  printData: string
  createdAt: string
  updatedAt: string
}

export interface PrintTemplateParams {
  code: string
  name: string
  templateData: string
  printData: string
}

export const printTemplateApi = {
  list() {
    return http.get<any, { code: number; message: string; data: PrintTemplateItem[] }>('/templates')
  },
  get(id: number) {
    return http.get<any, { code: number; message: string; data: PrintTemplateItem }>(`/templates/${id}`)
  },
  create(data: PrintTemplateParams) {
    return http.post<any, { code: number; message: string; data: PrintTemplateItem }>('/templates', data)
  },
  update(id: number, data: PrintTemplateParams) {
    return http.put<any, { code: number; message: string; data: PrintTemplateItem }>(`/templates/${id}`, data)
  },
  remove(id: number) {
    return http.delete(`/templates/${id}`)
  }
}
