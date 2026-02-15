<template>
  <div class="max-w-lg mx-auto">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-base-content">修改密码</h1>
      <p class="text-sm text-base-content/50 mt-1">更新您的登录密码以保障账户安全</p>
    </div>

    <div v-if="successMsg" class="alert alert-success mb-4 text-sm">
      <svg class="w-5 h-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
        <polyline points="22 4 12 14.01 9 11.01"/>
      </svg>
      <span>{{ successMsg }}</span>
    </div>

    <div class="bg-white rounded-lg border border-base-300 p-6">
      <form @submit.prevent="handleSubmit">
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text font-medium">旧密码</span>
          </label>
          <input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            class="input input-bordered w-full"
            required
          />
        </div>

        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text font-medium">新密码</span>
          </label>
          <input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            class="input input-bordered w-full"
            minlength="6"
            required
          />
        </div>

        <div class="form-control mb-6">
          <label class="label">
            <span class="label-text font-medium">确认新密码</span>
          </label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
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
            {{ loading ? '提交中...' : '修改密码' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { authApi } from '../api/auth'
import { useToastStore } from '../stores/toast'

const toast = useToastStore()

const form = ref({
  oldPassword: '',
  newPassword: ''
})
const confirmPassword = ref('')
const loading = ref(false)
const successMsg = ref('')

async function handleSubmit() {
  successMsg.value = ''

  if (form.value.newPassword !== confirmPassword.value) {
    toast.showError('两次输入的新密码不一致')
    return
  }

  loading.value = true
  try {
    await authApi.changePassword(form.value)
    successMsg.value = '密码修改成功'
    form.value = { oldPassword: '', newPassword: '' }
    confirmPassword.value = ''
  } catch { /* 拦截器已处理 */ } finally {
    loading.value = false
  }
}
</script>
