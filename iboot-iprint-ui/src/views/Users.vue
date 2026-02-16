<template>
  <div>
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-base-content">用户管理</h1>
        <p class="text-sm text-base-content/50 mt-1">管理系统登录用户</p>
      </div>
      <button class="btn btn-primary gap-2" @click="openCreateModal">
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        新建用户
      </button>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white rounded-lg border border-sidebar-border p-4 mb-4">
      <div class="flex items-center gap-3">
        <div class="relative flex-1 max-w-xs">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-base-content/30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input v-model="searchKeyword" type="text" placeholder="搜索用户名..." class="input input-bordered input-sm w-full pl-9" @keyup.enter="handleSearch" />
        </div>
        <button class="btn btn-sm btn-primary" @click="handleSearch">搜索</button>
        <button v-if="searchKeyword" class="btn btn-sm btn-ghost" @click="clearSearch">清除</button>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="bg-white rounded-lg border border-sidebar-border overflow-hidden">
      <table class="table">
        <thead>
          <tr class="bg-base-200/50">
            <th class="font-semibold text-base-content/70">ID</th>
            <th class="font-semibold text-base-content/70">用户名</th>
            <th class="font-semibold text-base-content/70">创建时间</th>
            <th class="font-semibold text-base-content/70">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="pageData.content.length === 0">
            <td colspan="4" class="text-center py-16">
              <div class="flex flex-col items-center gap-2">
                <svg class="w-12 h-12 text-base-content/20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                </svg>
                <p class="text-base-content/40 text-sm">暂无用户</p>
                <button class="btn btn-primary btn-sm mt-2" @click="openCreateModal">创建第一个用户</button>
              </div>
            </td>
          </tr>
          <tr v-for="item in pageData.content" :key="item.id" class="hover:bg-base-200/30 transition-colors duration-150">
            <td class="text-base-content/50">{{ item.id }}</td>
            <td class="font-medium text-base-content">{{ item.username }}</td>
            <td class="text-sm text-base-content/50">{{ item.createdAt }}</td>
            <td>
              <div class="flex gap-1">
                <button class="btn btn-ghost btn-xs gap-1 cursor-pointer" @click="openEditModal(item)">编辑</button>
                <button class="btn btn-ghost btn-xs gap-1 text-error hover:bg-error/10 cursor-pointer" @click="handleDelete(item)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div v-if="pageData.totalElements > 0" class="flex items-center justify-between px-4 py-3 border-t border-sidebar-border">
        <span class="text-sm text-base-content/50">共 {{ pageData.totalElements }} 条</span>
        <div class="flex items-center gap-2">
          <button class="btn btn-sm btn-ghost" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">上一页</button>
          <template v-for="p in displayPages" :key="p">
            <button v-if="p === '...'" class="btn btn-sm btn-ghost btn-disabled">...</button>
            <button v-else class="btn btn-sm" :class="p === currentPage ? 'btn-primary' : 'btn-ghost'" @click="goPage(p as number)">{{ p }}</button>
          </template>
          <button class="btn btn-sm btn-ghost" :disabled="currentPage >= pageData.totalPages" @click="goPage(currentPage + 1)">下一页</button>
        </div>
      </div>
    </div>

    <!-- 新建/编辑弹窗 -->
    <dialog ref="modalRef" class="modal">
      <div class="modal-box">
        <h3 class="text-lg font-bold mb-4">{{ isEditing ? '编辑用户' : '新建用户' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-control mb-4">
            <label class="label"><span class="label-text font-medium">用户名</span></label>
            <input v-model="form.username" type="text" placeholder="输入用户名" class="input input-bordered w-full" required />
          </div>
          <div class="form-control mb-6">
            <label class="label">
              <span class="label-text font-medium">密码</span>
              <span v-if="isEditing" class="label-text-alt text-base-content/40">留空则不修改</span>
            </label>
            <input v-model="form.password" type="password" :placeholder="isEditing ? '留空不修改密码' : '输入密码（至少6位）'" class="input input-bordered w-full" :required="!isEditing" minlength="6" />
          </div>
          <div class="modal-action">
            <button type="button" class="btn" @click="closeModal">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <span v-if="submitting" class="loading loading-spinner loading-sm"></span>
              {{ submitting ? '提交中...' : '确定' }}
            </button>
          </div>
        </form>
      </div>
      <form method="dialog" class="modal-backdrop"><button>close</button></form>
    </dialog>

    <!-- 删除确认弹窗 -->
    <dialog ref="deleteModalRef" class="modal">
      <div class="modal-box">
        <h3 class="text-lg font-bold">确认删除</h3>
        <p class="py-4">确定要删除用户 "{{ deleteTarget?.username }}" 吗？此操作不可恢复。</p>
        <div class="modal-action">
          <button class="btn" @click="deleteModalRef?.close()">取消</button>
          <button class="btn btn-error" @click="confirmDelete" :disabled="submitting">
            <span v-if="submitting" class="loading loading-spinner loading-sm"></span>
            删除
          </button>
        </div>
      </div>
      <form method="dialog" class="modal-backdrop"><button>close</button></form>
    </dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { userApi, type UserItem } from '../api/user'
import type { PageData } from '../api/printTemplate'
import { useToastStore } from '../stores/toast'

const toast = useToastStore()

const pageData = ref<PageData<UserItem>>({ content: [], totalElements: 0, totalPages: 0, page: 1, size: 10 })
const currentPage = ref(1)
const searchKeyword = ref('')
const activeKeyword = ref('')

const modalRef = ref<HTMLDialogElement>()
const deleteModalRef = ref<HTMLDialogElement>()
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const deleteTarget = ref<UserItem | null>(null)

const form = ref({ username: '', password: '' })

const displayPages = computed(() => {
  const total = pageData.value.totalPages
  const current = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const pages: (number | string)[] = [1]
  if (current > 3) pages.push('...')
  for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) pages.push(i)
  if (current < total - 2) pages.push('...')
  if (total > 1) pages.push(total)
  return pages
})

onMounted(() => { loadList() })

async function loadList() {
  try {
    const res = await userApi.list({ page: currentPage.value, size: 10, keyword: activeKeyword.value || undefined })
    pageData.value = res.data
  } catch { /* 拦截器已处理 */ }
}

function handleSearch() {
  activeKeyword.value = searchKeyword.value
  currentPage.value = 1
  loadList()
}

function clearSearch() {
  searchKeyword.value = ''
  activeKeyword.value = ''
  currentPage.value = 1
  loadList()
}

function goPage(page: number) {
  currentPage.value = page
  loadList()
}

function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  form.value = { username: '', password: '' }
  modalRef.value?.showModal()
}

function openEditModal(item: UserItem) {
  isEditing.value = true
  editingId.value = item.id
  form.value = { username: item.username, password: '' }
  modalRef.value?.showModal()
}

function closeModal() { modalRef.value?.close() }

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await userApi.update(editingId.value, form.value)
      toast.showSuccess('更新成功')
    } else {
      await userApi.create(form.value)
      toast.showSuccess('创建成功')
    }
    closeModal()
    await loadList()
  } catch { /* 拦截器已处理 */ } finally {
    submitting.value = false
  }
}

function handleDelete(item: UserItem) {
  deleteTarget.value = item
  deleteModalRef.value?.showModal()
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  submitting.value = true
  try {
    await userApi.remove(deleteTarget.value.id)
    toast.showSuccess('删除成功')
    deleteModalRef.value?.close()
    await loadList()
  } catch { /* 拦截器已处理 */ } finally {
    submitting.value = false
  }
}
</script>
