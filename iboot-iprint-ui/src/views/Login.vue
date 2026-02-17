<template>
  <div class="min-h-screen flex">
    <!-- 左侧品牌区域 -->
    <div class="hidden lg:flex lg:w-[55%] bg-gradient-to-br from-blue-600 via-blue-700 to-indigo-800 relative overflow-hidden flex-col justify-between p-12">
      <!-- 装饰背景 -->
      <div class="absolute inset-0">
        <div class="absolute top-[-10%] left-[-5%] w-96 h-96 bg-blue-400/20 rounded-full blur-3xl"></div>
        <div class="absolute bottom-[-10%] right-[-5%] w-[500px] h-[500px] bg-indigo-400/15 rounded-full blur-3xl"></div>
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-72 h-72 bg-blue-300/10 rounded-full blur-3xl"></div>
        <!-- 网格纹理 -->
        <div class="absolute inset-0 opacity-[0.03]" style="background-image: radial-gradient(circle, white 1px, transparent 1px); background-size: 24px 24px;"></div>
      </div>

      <!-- 顶部 Logo -->
      <div class="relative z-10">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-white/15 rounded-xl flex items-center justify-center backdrop-blur-sm">
            <svg class="w-6 h-6" viewBox="0 0 24 24" fill="none">
              <rect x="7" y="1.5" width="10" height="7" rx="1" fill="white" opacity="0.25" stroke="white" stroke-width="1.4"/>
              <rect x="3" y="8.5" width="18" height="7.5" rx="2" fill="white"/>
              <circle cx="16.5" cy="12.2" r="2" stroke="#2563eb" stroke-width="1.3" fill="none"/>
              <line x1="16.5" y1="10" x2="16.5" y2="11.6" stroke="#2563eb" stroke-width="1.3" stroke-linecap="round"/>
              <circle cx="7" cy="12.2" r="1.2" fill="#34d399"/>
              <rect x="7" y="16" width="10" height="6.5" rx="1" fill="white" opacity="0.25" stroke="white" stroke-width="1.4"/>
            </svg>
          </div>
          <span class="text-white/80 font-semibold text-lg tracking-wide">iBoot iPrint</span>
        </div>
      </div>

      <!-- 中部核心文案 -->
      <div class="relative z-10 max-w-lg">
        <div class="inline-flex items-center gap-2 px-3 py-1.5 bg-white/10 backdrop-blur-sm rounded-full mb-6">
          <span class="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"></span>
          <span class="text-blue-100 text-sm font-medium">开箱即用的打印中台</span>
        </div>
        <h1 class="text-5xl font-bold text-white mb-5 leading-tight">
          设计模版，<br/>
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-200 to-emerald-200">一键生成 PDF</span>
        </h1>
        <p class="text-blue-100/80 text-lg leading-relaxed mb-8">
          可视化拖拽设计标签、报表模版，通过 REST API 渲染生成 PDF。
          无需复杂配置，私有化部署，为您的业务系统赋予强大的打印能力。
        </p>

        <!-- 卖点标签 -->
        <div class="flex flex-wrap gap-3">
          <div class="flex items-center gap-2 px-3.5 py-2 bg-white/10 backdrop-blur-sm rounded-lg">
            <svg class="w-4 h-4 text-emerald-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M5 13l4 4L19 7"/></svg>
            <span class="text-white/90 text-sm">可视化模版设计</span>
          </div>
          <div class="flex items-center gap-2 px-3.5 py-2 bg-white/10 backdrop-blur-sm rounded-lg">
            <svg class="w-4 h-4 text-emerald-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M5 13l4 4L19 7"/></svg>
            <span class="text-white/90 text-sm">REST API 渲染</span>
          </div>
          <div class="flex items-center gap-2 px-3.5 py-2 bg-white/10 backdrop-blur-sm rounded-lg">
            <svg class="w-4 h-4 text-emerald-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M5 13l4 4L19 7"/></svg>
            <span class="text-white/90 text-sm">私有化部署</span>
          </div>
          <div class="flex items-center gap-2 px-3.5 py-2 bg-white/10 backdrop-blur-sm rounded-lg">
            <svg class="w-4 h-4 text-emerald-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M5 13l4 4L19 7"/></svg>
            <span class="text-white/90 text-sm">API Key 安全鉴权</span>
          </div>
        </div>
      </div>

      <!-- 底部 -->
      <div class="relative z-10">
        <p class="text-blue-200/40 text-sm">&copy; {{ new Date().getFullYear() }} iBoot. All rights reserved.</p>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="w-full lg:w-[45%] flex items-center justify-center bg-page-bg p-8">
      <div class="w-full max-w-sm">
        <!-- 移动端 Logo -->
        <div class="lg:hidden text-center mb-8">
          <div class="inline-flex items-center gap-3 mb-3">
            <svg class="w-9 h-9" viewBox="0 0 24 24" fill="none">
              <rect x="7" y="1.5" width="10" height="7" rx="1" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.4"/>
              <rect x="3" y="8.5" width="18" height="7.5" rx="2" fill="#3b82f6"/>
              <circle cx="16.5" cy="12.2" r="2" stroke="white" stroke-width="1.3" fill="none"/>
              <line x1="16.5" y1="10" x2="16.5" y2="11.6" stroke="white" stroke-width="1.3" stroke-linecap="round"/>
              <circle cx="7" cy="12.2" r="1.2" fill="#34d399"/>
              <rect x="7" y="16" width="10" height="6.5" rx="1" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.4"/>
            </svg>
            <span class="text-2xl font-bold text-base-content">iBoot iPrint</span>
          </div>
          <p class="text-sm text-base-content/50">开箱即用的打印中台</p>
        </div>

        <!-- 登录卡片 -->
        <div class="card bg-white shadow-xl shadow-black/5">
          <div class="card-body p-8">
            <div class="mb-6">
              <h2 class="text-2xl font-bold text-base-content mb-1.5">欢迎回来</h2>
              <p class="text-sm text-base-content/50">登录管理控制台，开始配置您的打印服务</p>
            </div>

            <form @submit.prevent="handleLogin">
              <div class="form-control mb-4">
                <label class="label">
                  <span class="label-text font-medium">用户名</span>
                </label>
                <input
                  v-model="username"
                  type="text"
                  placeholder="请输入用户名"
                  class="input input-bordered w-full focus:input-primary"
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
                  class="input input-bordered w-full focus:input-primary"
                  required
                />
              </div>

              <div class="form-control">
                <button
                  type="submit"
                  class="btn btn-primary w-full h-11"
                  :disabled="loading"
                >
                  <span v-if="loading" class="loading loading-spinner loading-sm"></span>
                  {{ loading ? '登录中...' : '进入控制台' }}
                </button>
              </div>
            </form>
          </div>
        </div>

        <p class="lg:hidden text-center text-xs text-base-content/30 mt-6">&copy; {{ new Date().getFullYear() }} iBoot. All rights reserved.</p>
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
