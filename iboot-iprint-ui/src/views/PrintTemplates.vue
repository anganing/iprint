<template>
  <div>
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">打印模版管理</h1>
      <button class="btn btn-primary" @click="openCreateModal">新建模版</button>
    </div>

    <!-- 模版列表 -->
    <div class="overflow-x-auto bg-base-100 rounded-lg shadow">
      <table class="table">
        <thead>
          <tr>
            <th>编码</th>
            <th>名称</th>
            <th>模版数据</th>
            <th>打印数据</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="list.length === 0">
            <td colspan="6" class="text-center opacity-50 py-8">暂无数据</td>
          </tr>
          <tr v-for="item in list" :key="item.id">
            <td><code class="text-sm bg-base-200 px-2 py-0.5 rounded">{{ item.code }}</code></td>
            <td class="font-medium">{{ item.name }}</td>
            <td class="max-w-40 truncate text-sm opacity-70">{{ item.templateData || '-' }}</td>
            <td class="max-w-40 truncate text-sm opacity-70">{{ item.printData || '-' }}</td>
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
      <div class="modal-box w-11/12 max-w-3xl">
        <h3 class="text-lg font-bold mb-4">{{ isEditing ? '编辑模版' : '新建模版' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="grid grid-cols-2 gap-4 mb-4">
            <div class="form-control">
              <label class="label"><span class="label-text">编码</span></label>
              <input
                v-model="form.code"
                type="text"
                placeholder="唯一编码"
                class="input input-bordered w-full"
                required
              />
            </div>
            <div class="form-control">
              <label class="label"><span class="label-text">名称</span></label>
              <input
                v-model="form.name"
                type="text"
                placeholder="模版名称"
                class="input input-bordered w-full"
                required
              />
            </div>
          </div>
          <div class="form-control mb-4">
            <label class="label"><span class="label-text">模版数据 (JSON)</span></label>
            <textarea
              v-model="form.templateData"
              placeholder='{"key": "value"}'
              class="textarea textarea-bordered w-full font-mono text-sm"
              rows="6"
            ></textarea>
          </div>
          <div class="form-control mb-6">
            <label class="label"><span class="label-text">打印数据 (JSON)</span></label>
            <textarea
              v-model="form.printData"
              placeholder='{"key": "value"}'
              class="textarea textarea-bordered w-full font-mono text-sm"
              rows="6"
            ></textarea>
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
        <p class="py-4">确定要删除模版 "{{ deleteTarget?.name }}" 吗？此操作不可恢复。</p>
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

    <!-- Toast -->
    <div v-if="toast" class="toast toast-end">
      <div class="alert" :class="toast.type === 'success' ? 'alert-success' : 'alert-error'">
        <span>{{ toast.message }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { printTemplateApi, type PrintTemplateItem } from '../api/printTemplate'

const list = ref<PrintTemplateItem[]>([])
const modalRef = ref<HTMLDialogElement>()
const deleteModalRef = ref<HTMLDialogElement>()
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const deleteTarget = ref<PrintTemplateItem | null>(null)
const toast = ref<{ message: string; type: 'success' | 'error' } | null>(null)

const form = ref({
  code: '',
  name: '',
  templateData: '',
  printData: ''
})

onMounted(() => {
  loadList()
})

async function loadList() {
  try {
    const res = await printTemplateApi.list()
    list.value = res.data
  } catch (e: any) {
    showToast(e.message || '加载失败', 'error')
  }
}

function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  form.value = { code: '', name: '', templateData: '', printData: '' }
  modalRef.value?.showModal()
}

function openEditModal(item: PrintTemplateItem) {
  isEditing.value = true
  editingId.value = item.id
  form.value = {
    code: item.code,
    name: item.name,
    templateData: item.templateData || '',
    printData: item.printData || ''
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
      await printTemplateApi.update(editingId.value, form.value)
      showToast('更新成功', 'success')
    } else {
      await printTemplateApi.create(form.value)
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

function handleDelete(item: PrintTemplateItem) {
  deleteTarget.value = item
  deleteModalRef.value?.showModal()
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  submitting.value = true
  try {
    await printTemplateApi.remove(deleteTarget.value.id)
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
