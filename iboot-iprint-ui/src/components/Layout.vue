<template>
  <div class="min-h-screen bg-page-bg">
    <!-- 顶栏 -->
    <header class="h-14 bg-white border-b border-sidebar-border sticky top-0 z-50">
      <div class="h-full max-w-7xl mx-auto px-6 flex items-center justify-between">
        <!-- 左侧：Logo + 导航 -->
        <div class="flex items-center gap-8">
          <div class="flex items-center gap-2.5">
            <svg class="w-6 h-6 text-primary shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 6 2 18 2 18 9"/>
              <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/>
              <rect x="6" y="14" width="12" height="8"/>
            </svg>
            <span class="text-base-content font-semibold text-base">iBoot iPrint</span>
            <span v-if="hiprintVersion" class="text-xs text-base-content/40 font-normal">{{ hiprintVersion }}</span>
          </div>
          <nav class="flex items-center gap-1">
            <router-link
              v-for="item in navItems"
              :key="item.path"
              :to="item.path"
              class="relative px-3 py-1.5 rounded-md text-sm font-medium transition-colors duration-200 cursor-pointer"
              :class="isActive(item.path)
                ? 'text-primary bg-primary/5'
                : 'text-nav-text hover:text-base-content hover:bg-nav-hover'"
            >
              {{ item.label }}
              <div
                v-if="isActive(item.path)"
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
  { path: '/templates', label: '打印模版' },
  { path: '/api-keys', label: 'API Key 管理' }
]

onMounted(async () => {
  try {
    const res = await http.get<any, { code: number; data: string }>('/engine/version')
    hiprintVersion.value = res.data
  } catch { /* ignore */ }
})

function isActive(path: string) {
  return route.path === path || route.path.startsWith(path + '/')
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>
