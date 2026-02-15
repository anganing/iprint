import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi, type UserInfo } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const isLoggedIn = ref(false)

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    user.value = res.data
    isLoggedIn.value = true
    return res
  }

  async function logout() {
    await authApi.logout()
    clearUser()
  }

  async function fetchUser() {
    try {
      const res = await authApi.getInfo()
      user.value = res.data
      isLoggedIn.value = true
    } catch {
      clearUser()
    }
  }

  function clearUser() {
    user.value = null
    isLoggedIn.value = false
  }

  return { user, isLoggedIn, login, logout, fetchUser, clearUser }
})
