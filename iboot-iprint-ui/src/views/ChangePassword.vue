<template>
  <div class="max-w-md mx-auto">
    <h1 class="text-2xl font-bold mb-6">修改密码</h1>

    <div v-if="successMsg" class="alert alert-success mb-4">
      <span>{{ successMsg }}</span>
    </div>
    <div v-if="errorMsg" class="alert alert-error mb-4">
      <span>{{ errorMsg }}</span>
    </div>

    <div class="card bg-base-100 shadow">
      <div class="card-body">
        <form @submit.prevent="handleSubmit">
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text">旧密码</span>
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
              <span class="label-text">新密码</span>
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
              <span class="label-text">确认新密码</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { authApi } from '../api/auth'

const form = ref({
  oldPassword: '',
  newPassword: ''
})
const confirmPassword = ref('')
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

async function handleSubmit() {
  errorMsg.value = ''
  successMsg.value = ''

  if (form.value.newPassword !== confirmPassword.value) {
    errorMsg.value = '两次输入的新密码不一致'
    return
  }

  loading.value = true
  try {
    await authApi.changePassword(form.value)
    successMsg.value = '密码修改成功'
    form.value = { oldPassword: '', newPassword: '' }
    confirmPassword.value = ''
  } catch (e: any) {
    errorMsg.value = e.message || '修改失败'
  } finally {
    loading.value = false
  }
}
</script>
