import http from './index'

export interface ApiKeyItem {
  id: number
  name: string
  apiKey: string
  status: number
  description: string
  createdAt: string
  updatedAt: string
}

export interface ApiKeyParams {
  name: string
  description: string
  status: number
}

export interface PageData<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface ApiKeyQuery {
  page?: number
  size?: number
  keyword?: string
  status?: number | null
}

export const apiKeyApi = {
  list(params: ApiKeyQuery = {}) {
    return http.get<any, { code: number; message: string; data: PageData<ApiKeyItem> }>('/keys', { params })
  },
  create(data: ApiKeyParams) {
    return http.post<any, { code: number; message: string; data: ApiKeyItem }>('/keys', data)
  },
  update(id: number, data: ApiKeyParams) {
    return http.put<any, { code: number; message: string; data: ApiKeyItem }>(`/keys/${id}`, data)
  },
  remove(id: number) {
    return http.delete(`/keys/${id}`)
  }
}
