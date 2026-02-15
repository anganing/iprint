<template>
  <div>
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">API Key 管理</h1>
      <button class="btn btn-primary" @click="openCreateModal">新建 API Key</button>
    </div>

    <!-- API Key 列表 -->
    <div class="overflow-x-auto bg-base-100 rounded-lg shadow">
      <table class="table">
        <thead>
          <tr>
            <th>名称</th>
            <th>API Key</th>
            <th>状态</th>
            <th>描述</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="list.length === 0">
            <td colspan="6" class="text-center opacity-50 py-8">暂无数据</td>
          </tr>
          <tr v-for="item in list" :key="item.id">
            <td class="font-medium">{{ item.name }}</td>
            <td>
              <div class="flex items-center gap-2">
                <code class="text-xs bg-base-200 px-2 py-1 rounded">
                  {{ maskedKey(item.apiKey) }}
                </code>
                <button class="btn btn-ghost btn-xs" @click="copyKey(item.apiKey)" title="复制">
                  📋
                </button>
              </div>
            </td>
            <td>
              <span
                class="badge"
                :class="item.status === 1 ? 'badge-success' : 'badge-error'"
              >
                {{ item.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="max-w-48 truncate">{{ item.description || '-' }}</td>
            <td class="text-sm opacity-70">{{ item.createdAt }}</td>
            <td>
              <div class="flex gap-1">
                <button class="btn btn-ghost btn-xs" @click="openEditModal(item)">编辑</button>
                <button class="btn btn-ghost btn-xs text-error" @click="handleDelete(item)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新建/编辑弹窗 -->
    <dialog ref="modalRef" class="modal">
      <div class="modal-box">
        <h3 class="text-lg font-bold mb-4">{{ isEditing ? '编辑 API Key' : '新建 API Key' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text">名称</span>
            </label>
            <input
              v-model="form.name"
              type="text"
              placeholder="输入名称"
              class="input input-bordered w-full"
              required
            />
          </div>
          <div class="form-control mb-4">
            <label class="label">
              <span class="label-text">描述</span>
            </label>
            <textarea
              v-model="form.description"
              placeholder="输入描述（可选）"
              class="textarea textarea-bordered w-full"
            ></textarea>
          </div>
          <div class="form-control mb-6">
            <label class="label">
              <span class="label-text">状态</span>
            </label>
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
      <form method="dialog" class="modal-backdrop">
        <button>close</button>
      </form>
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
      <form method="dialog" class="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>

    <!-- Toast 提示 -->
    <div v-if="toast" class="toast toast-end">
      <div class="alert" :class="toast.type === 'success' ? 'alert-success' : 'alert-error'">
        <span>{{ toast.message }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { apiKeyApi, type ApiKeyItem } from '../api/apiKey'

const list = ref<ApiKeyItem[]>([])
const modalRef = ref<HTMLDialogElement>()
const deleteModalRef = ref<HTMLDialogElement>()
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const deleteTarget = ref<ApiKeyItem | null>(null)
const toast = ref<{ message: string; type: 'success' | 'error' } | null>(null)

const form = ref({
  name: '',
  description: '',
  status: 1
})

onMounted(() => {
  loadList()
})

async function loadList() {
  try {
    const res = await apiKeyApi.list()
    list.value = res.data
  } catch (e: any) {
    showToast(e.message || '加载失败', 'error')
  }
}

function maskedKey(key: string) {
  if (key.length <= 12) return key
  return key.substring(0, 8) + '...' + key.substring(key.length - 4)
}

async function copyKey(key: string) {
  try {
    await navigator.clipboard.writeText(key)
    showToast('已复制到剪贴板', 'success')
  } catch {
    showToast('复制失败', 'error')
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
  form.value = {
    name: item.name,
    description: item.description || '',
    status: item.status
  }
  modalRef.value?.showModal()
}

function closeModal() {
  modalRef.value?.close()
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await apiKeyApi.update(editingId.value, form.value)
      showToast('更新成功', 'success')
    } else {
      await apiKeyApi.create(form.value)
      showToast('创建成功', 'success')
    }
    closeModal()
    await loadList()
  } catch (e: any) {
    showToast(e.message || '操作失败', 'error')
  } finally {
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
    showToast('删除成功', 'success')
    deleteModalRef.value?.close()
    await loadList()
  } catch (e: any) {
    showToast(e.message || '删除失败', 'error')
  } finally {
    submitting.value = false
  }
}

function showToast(message: string, type: 'success' | 'error') {
  toast.value = { message, type }
  setTimeout(() => {
    toast.value = null
  }, 3000)
}
</script>
