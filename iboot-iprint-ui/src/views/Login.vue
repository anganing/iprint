<template>
  <div class="min-h-screen bg-base-200 flex items-center justify-center">
    <div class="card bg-base-100 w-96 shadow-xl">
      <div class="card-body">
        <h2 class="card-title justify-center text-2xl mb-4">iBoot iPrint</h2>

        <div v-if="errorMsg" class="alert alert-error mb-4">
          <span>{{ errorMsg }}</span>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text">用户名</span>
            </label>
            <input
              v-model="username"
              type="text"
              placeholder="请输入用户名"
              class="input input-bordered w-full"
              required
            />
          </div>

          <div class="form-control mb-6">
            <label class="label">
              <span class="label-text">密码</span>
            </label>
            <input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              class="input input-bordered w-full"
              required
            />
          </div>

          <div class="form-control">
            <button
              type="submit"
              class="btn btn-primary w-full"
              :disabled="loading"
            >
              <span v-if="loading" class="loading loading-spinner loading-sm"></span>
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  errorMsg.value = ''
  loading.value = true
  try {
    await authStore.login(username.value, password.value)
    router.push('/')
  } catch (e: any) {
    errorMsg.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>
