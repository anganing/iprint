import axios from 'axios'
import router from '../router'
import { useToastStore } from '../stores/toast'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

http.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        router.push('/login')
        return Promise.reject(new Error(res.message || '未授权'))
      }
      const msg = res.message || '请求失败'
      const toastStore = useToastStore()
      toastStore.showError(msg)
      return Promise.reject(new Error(msg))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      if (error.config?.url?.includes('/auth/login')) {
        const toastStore = useToastStore()
        toastStore.showError('用户名或密码错误')
      } else {
        router.push('/login')
      }
      return Promise.reject(error)
    }
    let msg = '请求失败'
    if (!error.response) {
      msg = '网络连接失败，请检查网络'
    } else if (error.response.status >= 500) {
      msg = '服务器错误，请稍后重试'
    } else if (error.response.status === 403) {
      msg = '无权访问'
    } else if (error.response.status === 404) {
      msg = '请求的资源不存在'
    }
    const toastStore = useToastStore()
    toastStore.showError(msg)
    return Promise.reject(error)
  }
)

export default http
