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

export interface PageData<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface PrintTemplateQuery {
  page?: number
  size?: number
  keyword?: string
}

export const printTemplateApi = {
  list(params: PrintTemplateQuery = {}) {
    return http.get<any, { code: number; message: string; data: PageData<PrintTemplateItem> }>('/templates', { params })
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
