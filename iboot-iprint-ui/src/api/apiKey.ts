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

export const apiKeyApi = {
  list() {
    return http.get<any, { code: number; message: string; data: ApiKeyItem[] }>('/keys')
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
