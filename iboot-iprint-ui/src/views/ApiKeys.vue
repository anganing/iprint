<template>
  <div>
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-base-content">API Key 管理</h1>
        <p class="text-sm text-base-content/50 mt-1">创建和管理您的 API 访问密钥</p>
      </div>
      <button class="btn btn-primary gap-2" @click="openCreateModal">
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        新建 API Key
      </button>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white rounded-lg border border-sidebar-border p-4 mb-4">
      <div class="flex items-center gap-3">
        <div class="relative flex-1 max-w-xs">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-base-content/30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input v-model="searchKeyword" type="text" placeholder="搜索名称或描述..." class="input input-bordered input-sm w-full pl-9" @keyup.enter="handleSearch" />
        </div>
        <select v-model="searchStatus" class="select select-bordered select-sm w-28" @change="handleSearch">
          <option :value="null">全部状态</option>
          <option :value="1">启用</option>
          <option :value="0">禁用</option>
        </select>
        <button class="btn btn-sm btn-primary" @click="handleSearch">搜索</button>
        <button v-if="searchKeyword || searchStatus !== null" class="btn btn-sm btn-ghost" @click="clearSearch">清除</button>
      </div>
    </div>

    <!-- API Key 列表 -->
    <div class="bg-white rounded-lg border border-sidebar-border overflow-hidden">
      <table class="table">
        <thead>
          <tr class="bg-base-200/50">
            <th class="font-semibold text-base-content/70">名称</th>
            <th class="font-semibold text-base-content/70">API Key</th>
            <th class="font-semibold text-base-content/70">状态</th>
            <th class="font-semibold text-base-content/70">描述</th>
            <th class="font-semibold text-base-content/70">创建时间</th>
            <th class="font-semibold text-base-content/70">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="pageData.content.length === 0">
            <td colspan="6" class="text-center py-16">
              <div class="flex flex-col items-center gap-2">
                <svg class="w-12 h-12 text-base-content/20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
                </svg>
                <p class="text-base-content/40 text-sm">暂无 API Key</p>
                <button class="btn btn-primary btn-sm mt-2" @click="openCreateModal">创建第一个 API Key</button>
              </div>
            </td>
          </tr>
          <tr v-for="item in pageData.content" :key="item.id" class="hover:bg-base-200/30 transition-colors duration-150">
            <td class="font-medium text-base-content">{{ item.name }}</td>
            <td>
              <div class="flex items-center gap-2">
                <code class="text-xs bg-base-200 px-2 py-1 rounded font-mono">{{ maskedKey(item.apiKey) }}</code>
                <button class="btn btn-ghost btn-xs cursor-pointer hover:bg-primary/10" @click="copyKey(item.apiKey)" title="复制">
                  <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                  </svg>
                </button>
              </div>
            </td>
            <td>
              <span class="badge badge-sm gap-1" :class="item.status === 1 ? 'badge-success' : 'badge-error'">
                {{ item.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="max-w-48 truncate text-sm text-base-content/50">{{ item.description || '-' }}</td>
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
        <h3 class="text-lg font-bold mb-4">{{ isEditing ? '编辑 API Key' : '新建 API Key' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-control mb-4">
            <label class="label"><span class="label-text font-medium">名称</span></label>
            <input v-model="form.name" type="text" placeholder="输入名称" class="input input-bordered w-full" required />
          </div>
          <div class="form-control mb-4">
            <label class="label"><span class="label-text font-medium">描述</span></label>
            <textarea v-model="form.description" placeholder="输入描述（可选）" class="textarea textarea-bordered w-full"></textarea>
          </div>
          <div class="form-control mb-6">
            <label class="label"><span class="label-text font-medium">状态</span></label>
            <select v-model="form.status" class="select select-bordered w-full">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
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
        <p class="py-4">确定要删除 API Key "{{ deleteTarget?.name }}" 吗？此操作不可恢复。</p>
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
import { apiKeyApi, type ApiKeyItem, type PageData } from '../api/apiKey'
import { useToastStore } from '../stores/toast'

const toast = useToastStore()

const pageData = ref<PageData<ApiKeyItem>>({ content: [], totalElements: 0, totalPages: 0, page: 1, size: 10 })
const currentPage = ref(1)
const searchKeyword = ref('')
const searchStatus = ref<number | null>(null)
const activeKeyword = ref('')
const activeStatus = ref<number | null>(null)

const modalRef = ref<HTMLDialogElement>()
const deleteModalRef = ref<HTMLDialogElement>()
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const deleteTarget = ref<ApiKeyItem | null>(null)

const form = ref({ name: '', description: '', status: 1 })

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
    const params: Record<string, any> = { page: currentPage.value, size: 10 }
    if (activeKeyword.value) params.keyword = activeKeyword.value
    if (activeStatus.value !== null) params.status = activeStatus.value
    const res = await apiKeyApi.list(params)
    pageData.value = res.data
  } catch { /* 拦截器已处理 */ }
}

function handleSearch() {
  activeKeyword.value = searchKeyword.value
  activeStatus.value = searchStatus.value
  currentPage.value = 1
  loadList()
}

function clearSearch() {
  searchKeyword.value = ''
  searchStatus.value = null
  activeKeyword.value = ''
  activeStatus.value = null
  currentPage.value = 1
  loadList()
}

function goPage(page: number) {
  currentPage.value = page
  loadList()
}

function maskedKey(key: string) {
  if (key.length <= 12) return key
  return key.substring(0, 8) + '...' + key.substring(key.length - 4)
}

async function copyKey(key: string) {
  try {
    await navigator.clipboard.writeText(key)
    toast.showSuccess('已复制到剪贴板')
  } catch {
    toast.showError('复制失败')
  }
}

function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  form.value = { name: '', description: '', status: 1 }
  modalRef.value?.showModal()
}

function openEditModal(item: ApiKeyItem) {
  isEditing.value = true
  editingId.value = item.id
  form.value = { name: item.name, description: item.description || '', status: item.status }
  modalRef.value?.showModal()
}

function closeModal() { modalRef.value?.close() }

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await apiKeyApi.update(editingId.value, form.value)
      toast.showSuccess('更新成功')
    } else {
      await apiKeyApi.create(form.value)
      toast.showSuccess('创建成功')
    }
    closeModal()
    await loadList()
  } catch { /* 拦截器已处理 */ } finally {
    submitting.value = false
  }
}

function handleDelete(item: ApiKeyItem) {
  deleteTarget.value = item
  deleteModalRef.value?.showModal()
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  submitting.value = true
  try {
    await apiKeyApi.remove(deleteTarget.value.id)
    toast.showSuccess('删除成功')
    deleteModalRef.value?.close()
    await loadList()
  } catch { /* 拦截器已处理 */ } finally {
    submitting.value = false
  }
}
</script>
