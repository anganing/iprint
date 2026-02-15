<template>
  <div class="min-h-screen flex">
    <!-- 左侧品牌区域 -->
    <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-blue-600 to-blue-800 relative overflow-hidden flex-col items-center justify-center p-12">
      <!-- 装饰背景 -->
      <div class="absolute inset-0 opacity-15">
        <div class="absolute top-20 left-20 w-72 h-72 bg-blue-400 rounded-full blur-3xl"></div>
        <div class="absolute bottom-20 right-20 w-96 h-96 bg-blue-300 rounded-full blur-3xl"></div>
      </div>
      <!-- 品牌内容 -->
      <div class="relative z-10 text-center">
        <div class="mb-8 flex justify-center">
          <div class="w-20 h-20 bg-white/15 rounded-2xl flex items-center justify-center">
            <svg class="w-10 h-10 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 6 2 18 2 18 9"/>
              <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/>
              <rect x="6" y="14" width="12" height="8"/>
            </svg>
          </div>
        </div>
        <h1 class="text-4xl font-bold text-white mb-4">iBoot iPrint</h1>
        <p class="text-blue-100 text-lg max-w-sm">轻量级打印模版管理系统，高效管理您的打印模版与 API 密钥</p>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="w-full lg:w-1/2 flex items-center justify-center bg-page-bg p-8">
      <div class="w-full max-w-sm">
        <!-- 移动端 Logo -->
        <div class="lg:hidden text-center mb-8">
          <div class="inline-flex items-center gap-3 mb-2">
            <svg class="w-8 h-8 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 6 2 18 2 18 9"/>
              <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/>
              <rect x="6" y="14" width="12" height="8"/>
            </svg>
            <span class="text-2xl font-bold text-base-content">iBoot iPrint</span>
          </div>
        </div>

        <!-- 登录卡片 -->
        <div class="card bg-white shadow-lg">
          <div class="card-body p-8">
            <h2 class="text-2xl font-bold text-base-content mb-1">欢迎登录</h2>
            <p class="text-sm text-base-content/50 mb-6">请输入您的账号信息</p>

            <form @submit.prevent="handleLogin">
              <div class="form-control mb-4">
                <label class="label">
                  <span class="label-text font-medium">用户名</span>
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
                  <span class="label-text font-medium">密码</span>
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

        <p class="text-center text-xs text-base-content/40 mt-6">iBoot iPrint &copy; {{ new Date().getFullYear() }}</p>
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

async function handleLogin() {
  loading.value = true
  try {
    await authStore.login(username.value, password.value)
    router.push('/')
  } catch { /* 拦截器已处理 */ } finally {
    loading.value = false
  }
}
</script>
