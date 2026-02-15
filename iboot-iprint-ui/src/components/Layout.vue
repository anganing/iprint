<template>
  <div class="min-h-screen bg-base-200">
    <!-- 顶部导航栏 -->
    <div class="navbar bg-base-100 shadow-sm">
      <div class="flex-1">
        <a class="btn btn-ghost text-xl">iBoot iPrint</a>
      </div>
      <div class="flex-none gap-2">
        <router-link to="/api-keys" class="btn btn-ghost btn-sm" active-class="btn-active">
          API Key 管理
        </router-link>
        <router-link to="/templates" class="btn btn-ghost btn-sm" active-class="btn-active">
          打印模版
        </router-link>
        <router-link to="/change-password" class="btn btn-ghost btn-sm" active-class="btn-active">
          修改密码
        </router-link>
        <div class="divider divider-horizontal m-0"></div>
        <span class="text-sm opacity-70">{{ authStore.user?.username }}</span>
        <button class="btn btn-ghost btn-sm" @click="handleLogout">退出</button>
      </div>
    </div>

    <!-- 页面内容 -->
    <div class="container mx-auto p-6 max-w-6xl">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>
