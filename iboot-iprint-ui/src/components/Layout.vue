<template>
  <div class="min-h-screen bg-page-bg">
    <!-- 顶栏 -->
    <header class="h-14 bg-white border-b border-sidebar-border sticky top-0 z-50">
      <div class="h-full max-w-7xl mx-auto px-6 flex items-center justify-between">
        <!-- 左侧：Logo + 导航 -->
        <div class="flex items-center gap-8">
          <router-link to="/" class="flex items-center gap-2.5 hover:opacity-80 transition-opacity cursor-pointer">
            <svg class="w-7 h-7 shrink-0" viewBox="0 0 24 24" fill="none">
              <!-- 进纸 -->
              <rect x="7" y="1.5" width="10" height="7" rx="1" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.4"/>
              <line x1="9.5" y1="4" x2="14.5" y2="4" stroke="#3b82f6" stroke-width="0.8" stroke-linecap="round" opacity="0.25"/>
              <line x1="9.5" y1="6" x2="13" y2="6" stroke="#3b82f6" stroke-width="0.8" stroke-linecap="round" opacity="0.25"/>
              <!-- 打印机主体 -->
              <rect x="3" y="8.5" width="18" height="7.5" rx="2" fill="#3b82f6"/>
              <!-- 电源符号 ⏻ (iBoot) -->
              <circle cx="16.5" cy="12.2" r="2" stroke="white" stroke-width="1.3" fill="none"/>
              <line x1="16.5" y1="10" x2="16.5" y2="11.6" stroke="white" stroke-width="1.3" stroke-linecap="round"/>
              <!-- 绿色指示灯 -->
              <circle cx="7" cy="12.2" r="1.2" fill="#34d399"/>
              <!-- 出纸 -->
              <rect x="7" y="16" width="10" height="6.5" rx="1" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.4"/>
              <line x1="9.5" y1="19" x2="14.5" y2="19" stroke="#3b82f6" stroke-width="0.8" stroke-linecap="round" opacity="0.25"/>
              <line x1="9.5" y1="21" x2="13" y2="21" stroke="#3b82f6" stroke-width="0.8" stroke-linecap="round" opacity="0.25"/>
            </svg>
            <span class="text-base-content font-semibold text-base">iBoot iPrint</span>
            <span v-if="hiprintVersion" class="text-xs text-base-content/40 font-normal">{{ hiprintVersion }}</span>
          </router-link>
          <nav class="flex items-center gap-1">
            <router-link
              v-for="item in navItems"
              :key="item.path"
              :to="item.path"
              class="relative px-3 py-1.5 rounded-md text-sm font-medium transition-colors duration-200 cursor-pointer"
              :class="isActive(item)
                ? 'text-primary bg-primary/5'
                : 'text-nav-text hover:text-base-content hover:bg-nav-hover'"
            >
              {{ item.label }}
              <div
                v-if="isActive(item)"
                class="absolute bottom-0 left-3 right-3 h-0.5 bg-primary rounded-t translate-y-[11px]"
              ></div>
            </router-link>
          </nav>
        </div>

        <!-- 右侧：用户信息 -->
        <div class="flex items-center gap-3">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center">
              <svg class="w-3.5 h-3.5 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <span class="text-sm text-base-content/70">{{ authStore.user?.username }}</span>
          </div>
          <div class="w-px h-5 bg-sidebar-border"></div>
          <router-link
            to="/change-password"
            class="text-sm text-nav-text hover:text-base-content transition-colors duration-200 cursor-pointer"
          >
            修改密码
          </router-link>
          <div class="w-px h-5 bg-sidebar-border"></div>
          <button
            class="text-sm text-nav-text hover:text-error transition-colors duration-200 cursor-pointer"
            @click="handleLogout"
          >
            退出
          </button>
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="max-w-7xl mx-auto px-6 py-6">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="py-4 text-center text-xs text-base-content/30">
      &copy; {{ new Date().getFullYear() }} iBoot. All rights reserved.
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter, useRoute } from 'vue-router'
import http from '../api/index'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const hiprintVersion = ref('')

const navItems = [
  { path: '/', label: '首页', exact: true },
  { path: '/templates', label: '打印模版', exact: false },
  { path: '/api-keys', label: 'API Key 管理', exact: false },
  { path: '/users', label: '用户管理', exact: false }
]

onMounted(async () => {
  try {
    const res = await http.get<any, { code: number; data: string }>('/engine/version')
    hiprintVersion.value = res.data
  } catch { /* ignore */ }
})

function isActive(item: { path: string; exact: boolean }) {
  if (item.exact) return route.path === item.path
  return route.path === item.path || route.path.startsWith(item.path + '/')
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>
