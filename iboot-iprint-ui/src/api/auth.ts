import http from './index'

export interface LoginParams {
  username: string
  password: string
}

export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
}

export interface UserInfo {
  id: number
  username: string
}

export const authApi = {
  login(data: LoginParams) {
    return http.post<any, { code: number; message: string; data: UserInfo }>('/auth/login', data)
  },
  logout() {
    return http.post('/auth/logout')
  },
  getInfo() {
    return http.get<any, { code: number; message: string; data: UserInfo }>('/auth/info')
  },
  changePassword(data: ChangePasswordParams) {
    return http.put('/auth/password', data)
  }
}
