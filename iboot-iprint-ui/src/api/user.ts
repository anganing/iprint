import http from './index'
import type { PageData } from './printTemplate'

export interface UserItem {
  id: number
  username: string
  createdAt: string
}

export interface UserRequest {
  username: string
  password: string
}

export interface UserQuery {
  page?: number
  size?: number
  keyword?: string
}

export const userApi = {
  list(params: UserQuery = {}) {
    return http.get<any, { code: number; message: string; data: PageData<UserItem> }>('/users', { params })
  },
  create(data: UserRequest) {
    return http.post<any, { code: number; message: string; data: UserItem }>('/users', data)
  },
  update(id: number, data: UserRequest) {
    return http.put<any, { code: number; message: string; data: UserItem }>(`/users/${id}`, data)
  },
  remove(id: number) {
    return http.delete(`/users/${id}`)
  }
}
