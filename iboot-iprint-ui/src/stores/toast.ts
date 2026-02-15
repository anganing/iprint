import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface ToastMessage {
  id: number
  message: string
  type: 'success' | 'error'
}

let nextId = 0

export const useToastStore = defineStore('toast', () => {
  const messages = ref<ToastMessage[]>([])

  function show(message: string, type: 'success' | 'error') {
    const id = ++nextId
    messages.value.push({ id, message, type })
    setTimeout(() => {
      remove(id)
    }, 3000)
  }

  function showSuccess(message: string) {
    show(message, 'success')
  }

  function showError(message: string) {
    show(message, 'error')
  }

  function remove(id: number) {
    messages.value = messages.value.filter(m => m.id !== id)
  }

  return { messages, showSuccess, showError, remove }
})
