<template>
  <div>
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-base-content">打印模版管理</h1>
        <p class="text-sm text-base-content/50 mt-1">管理和预览您的打印模版</p>
      </div>
      <button class="btn btn-primary gap-2" @click="openCreateModal">
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        新建模版
      </button>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white rounded-lg border border-sidebar-border p-4 mb-4">
      <div class="flex items-center gap-3">
        <div class="relative flex-1 max-w-xs">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-base-content/30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索编码或名称..."
            class="input input-bordered input-sm w-full pl-9"
            @keyup.enter="handleSearch"
          />
        </div>
        <button class="btn btn-sm btn-primary" @click="handleSearch">搜索</button>
        <button v-if="searchKeyword" class="btn btn-sm btn-ghost" @click="clearSearch">清除</button>
      </div>
    </div>

    <!-- 模版列表 -->
    <div class="bg-white rounded-lg border border-sidebar-border overflow-hidden">
      <table class="table">
        <thead>
          <tr class="bg-base-200/50">
            <th class="font-semibold text-base-content/70">编码</th>
            <th class="font-semibold text-base-content/70">名称</th>
            <th class="font-semibold text-base-content/70">模版数据</th>
            <th class="font-semibold text-base-content/70">打印数据</th>
            <th class="font-semibold text-base-content/70">创建时间</th>
            <th class="font-semibold text-base-content/70">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="pageData.content.length === 0">
            <td colspan="6" class="text-center py-16">
              <div class="flex flex-col items-center gap-2">
                <svg class="w-12 h-12 text-base-content/20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
                </svg>
                <p class="text-base-content/40 text-sm">暂无模版数据</p>
                <button class="btn btn-primary btn-sm mt-2" @click="openCreateModal">创建第一个模版</button>
              </div>
            </td>
          </tr>
          <tr v-for="item in pageData.content" :key="item.id" class="hover:bg-base-200/30 transition-colors duration-150">
            <td><code class="text-xs bg-primary/5 text-primary px-2 py-1 rounded font-mono">{{ item.code }}</code></td>
            <td class="font-medium text-base-content">{{ item.name }}</td>
            <td>
              <span v-if="item.templateData" class="inline-flex items-center gap-1 text-xs text-success">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                已配置
              </span>
              <span v-else class="text-xs text-base-content/40">-</span>
            </td>
            <td>
              <span v-if="item.printData" class="inline-flex items-center gap-1 text-xs text-success">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                已配置
              </span>
              <span v-else class="text-xs text-base-content/40">-</span>
            </td>
            <td class="text-sm text-base-content/50">{{ item.createdAt }}</td>
            <td>
              <div class="flex gap-1">
                <button class="btn btn-ghost btn-xs gap-1 cursor-pointer hover:bg-base-200" @click="showCurlModal(item)" title="查看调用示例">
                  <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
                  curl
                </button>
                <button class="btn btn-ghost btn-xs gap-1 text-primary hover:bg-primary/10 cursor-pointer" @click="handlePreview(item)">预览</button>
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
      <div class="modal-box w-11/12 max-w-6xl max-h-[90vh] flex flex-col">
        <h3 class="text-lg font-bold mb-4">{{ isEditing ? '编辑模版' : '新建模版' }}</h3>
        <form @submit.prevent="handleSubmit" class="flex flex-col flex-1 min-h-0">
          <div class="grid grid-cols-2 gap-4 mb-4">
            <div class="form-control">
              <label class="label"><span class="label-text font-medium">编码</span></label>
              <input v-model="form.code" type="text" placeholder="唯一编码" class="input input-bordered w-full" required />
            </div>
            <div class="form-control">
              <label class="label"><span class="label-text font-medium">名称</span></label>
              <input v-model="form.name" type="text" placeholder="模版名称" class="input input-bordered w-full" required />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4 flex-1 min-h-0 mb-4">
            <div class="form-control flex flex-col min-h-0">
              <label class="label"><span class="label-text font-medium">模版数据 (JSON)</span></label>
              <div class="flex-1 min-h-0">
                <JsonEditor v-model="form.templateData" />
              </div>
            </div>
            <div class="form-control flex flex-col min-h-0">
              <label class="label"><span class="label-text font-medium">打印数据 (JSON)</span></label>
              <div class="flex-1 min-h-0">
                <JsonEditor v-model="form.printData" />
              </div>
            </div>
          </div>
          <div class="modal-action mt-0 pt-4 border-t border-sidebar-border">
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
        <p class="py-4">确定要删除模版 "{{ deleteTarget?.name }}" 吗？此操作不可恢复。</p>
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

    <!-- PDF 预览弹窗 -->
    <dialog ref="previewModalRef" class="modal">
      <div class="modal-box w-11/12 max-w-6xl h-[85vh] flex flex-col p-0">
        <div class="flex items-center justify-between px-6 py-4 border-b border-sidebar-border">
          <h3 class="text-lg font-bold">预览: {{ previewTarget?.name }}</h3>
          <button class="btn btn-sm btn-ghost cursor-pointer" @click="closePreview">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="flex-1 overflow-hidden">
          <div v-if="previewLoading" class="flex items-center justify-center h-full">
            <span class="loading loading-spinner loading-lg"></span>
            <span class="ml-3">正在生成 PDF...</span>
          </div>
          <PDFViewer v-else-if="previewPdfUrl" :config="{ src: previewPdfUrl }" :style="{ width: '100%', height: '100%' }" />
        </div>
      </div>
      <form method="dialog" class="modal-backdrop"><button @click="closePreview">close</button></form>
    </dialog>

    <!-- curl 示例弹窗 -->
    <dialog ref="curlModalRef" class="modal">
      <div class="modal-box w-11/12 max-w-3xl">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold">API 调用示例</h3>
          <button class="btn btn-sm btn-ghost cursor-pointer" @click="curlModalRef?.close()">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="tabs tabs-bordered mb-4">
          <a class="tab" :class="curlTab === 'pdf' ? 'tab-active' : ''" @click="curlTab = 'pdf'">生成 PDF</a>
          <a class="tab" :class="curlTab === 'html' ? 'tab-active' : ''" @click="curlTab = 'html'">生成 HTML</a>
        </div>
        <div class="relative">
          <pre class="bg-gray-900 text-gray-100 rounded-lg p-4 text-sm font-mono whitespace-pre-wrap break-all overflow-auto max-h-80 leading-relaxed">{{ curlCommand }}</pre>
          <button class="btn btn-xs btn-ghost absolute top-2 right-2 text-gray-400 hover:text-white cursor-pointer" @click="copyCurl">
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
            </svg>
            复制
          </button>
        </div>
        <p class="text-xs text-base-content/40 mt-3">请将 <code class="bg-base-200 px-1 rounded">your-api-key</code> 替换为实际的 API Key</p>
      </div>
      <form method="dialog" class="modal-backdrop"><button>close</button></form>
    </dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { printTemplateApi, type PrintTemplateItem, type PageData } from '../api/printTemplate'
import { engineApi } from '../api/engine'
import { useToastStore } from '../stores/toast'
import { PDFViewer } from '@embedpdf/vue-pdf-viewer'
import JsonEditor from '../components/JsonEditor.vue'

const toast = useToastStore()

const pageData = ref<PageData<PrintTemplateItem>>({ content: [], totalElements: 0, totalPages: 0, page: 1, size: 10 })
const currentPage = ref(1)
const searchKeyword = ref('')
const activeKeyword = ref('')

const modalRef = ref<HTMLDialogElement>()
const deleteModalRef = ref<HTMLDialogElement>()
const previewModalRef = ref<HTMLDialogElement>()
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const deleteTarget = ref<PrintTemplateItem | null>(null)

const previewPdfUrl = ref<string | null>(null)
const previewLoading = ref(false)
const previewTarget = ref<PrintTemplateItem | null>(null)

const curlModalRef = ref<HTMLDialogElement>()
const curlTab = ref<'pdf' | 'html'>('pdf')
const curlTarget = ref<PrintTemplateItem | null>(null)

const form = ref({ code: '', name: '', templateData: '', printData: '' })

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
    const res = await printTemplateApi.list({ page: currentPage.value, size: 10, keyword: activeKeyword.value || undefined })
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
  form.value = { code: '', name: '', templateData: '', printData: '' }
  modalRef.value?.showModal()
}

function openEditModal(item: PrintTemplateItem) {
  isEditing.value = true
  editingId.value = item.id
  form.value = { code: item.code, name: item.name, templateData: item.templateData || '', printData: item.printData || '' }
  modalRef.value?.showModal()
}

function closeModal() { modalRef.value?.close() }

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await printTemplateApi.update(editingId.value, form.value)
      toast.showSuccess('更新成功')
    } else {
      await printTemplateApi.create(form.value)
      toast.showSuccess('创建成功')
    }
    closeModal()
    await loadList()
  } catch { /* 拦截器已处理 */ } finally {
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
    toast.showSuccess('删除成功')
    deleteModalRef.value?.close()
    await loadList()
  } catch { /* 拦截器已处理 */ } finally {
    submitting.value = false
  }
}

async function handlePreview(item: PrintTemplateItem) {
  if (!item.templateData || !item.printData) {
    toast.showError('模版数据或打印数据为空，无法预览')
    return
  }
  let tplData: object
  let printData: object[]
  try {
    tplData = JSON.parse(item.templateData)
    printData = JSON.parse(item.printData)
  } catch {
    toast.showError('模版数据或打印数据 JSON 格式错误')
    return
  }
  if (Array.isArray(tplData) || typeof tplData !== 'object') {
    toast.showError('模版数据必须是 JSON 对象')
    return
  }
  if (!Array.isArray(printData)) {
    toast.showError('打印数据必须是 JSON 数组')
    return
  }

  previewTarget.value = item
  previewLoading.value = true
  previewPdfUrl.value = null
  previewModalRef.value?.showModal()

  try {
    const blob = await engineApi.generatePdf(tplData, printData)
    previewPdfUrl.value = URL.createObjectURL(blob)
  } catch (e: any) {
    closePreview()
    toast.showError(e.message || 'PDF 生成失败')
  } finally {
    previewLoading.value = false
  }
}

function closePreview() {
  previewModalRef.value?.close()
  if (previewPdfUrl.value) {
    URL.revokeObjectURL(previewPdfUrl.value)
    previewPdfUrl.value = null
  }
  previewTarget.value = null
}

function showCurlModal(item: PrintTemplateItem) {
  curlTarget.value = item
  curlTab.value = 'pdf'
  curlModalRef.value?.showModal()
}

const curlCommand = computed(() => {
  if (!curlTarget.value) return ''
  const endpoint = curlTab.value === 'pdf' ? '/api/engine/render/pdf' : '/api/engine/render/html'
  const outFlag = curlTab.value === 'pdf' ? ' -o output.pdf' : ''
  let printData = '[]'
  if (curlTarget.value.printData) {
    try {
      printData = JSON.stringify(JSON.parse(curlTarget.value.printData))
    } catch { /* keep default */ }
  }
  const body = JSON.stringify({ code: curlTarget.value.code, printData: JSON.parse(printData) })
  return `curl -X POST '${location.origin}${endpoint}' \\
  -H 'Content-Type: application/json' \\
  -H 'X-API-Key: your-api-key' \\
  -d '${body}'${outFlag}`
})

async function copyCurl() {
  try {
    await navigator.clipboard.writeText(curlCommand.value)
    toast.showSuccess('已复制到剪贴板')
  } catch {
    toast.showError('复制失败')
  }
}
</script>
