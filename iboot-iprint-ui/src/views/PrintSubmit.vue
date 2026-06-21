<template>
  <div>
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-base-content">提交打印</h1>
        <p class="text-sm text-base-content/50 mt-1">选择模板和打印机，提交打印任务</p>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 表单 -->
      <div class="bg-white rounded-lg border border-sidebar-border p-6">
        <h3 class="font-semibold text-base-content mb-4 flex items-center gap-2">
          <svg class="w-5 h-5 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
          </svg>
          打印参数
        </h3>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div class="form-control">
            <label class="label"><span class="label-text font-medium">打印模板 <span class="text-error">*</span></span></label>
            <select v-model="form.templateCode" class="select select-bordered w-full" :disabled="loading" required>
              <option value="" disabled>请选择模板</option>
              <option v-for="tpl in templates" :key="tpl.code" :value="tpl.code">{{ tpl.name }} ({{ tpl.code }})</option>
            </select>
            <p v-if="templatesLoading" class="text-xs text-base-content/40 mt-1">加载模板中...</p>
          </div>

          <div class="form-control">
            <label class="label"><span class="label-text font-medium">打印机 <span class="text-error">*</span></span></label>
            <select v-model="form.printerName" class="select select-bordered w-full" :disabled="loading" required>
              <option value="" disabled>请选择打印机</option>
              <option v-for="p in printers" :key="p.name" :value="p.name">{{ p.name }} <span v-if="p.isDefault">(默认)</span> - {{ p.location || p.state }}</option>
            </select>
            <button type="button" class="text-xs text-primary mt-1 self-start cursor-pointer hover:underline" @click="loadPrinters" :disabled="printersLoading">
              {{ printersLoading ? '刷新中...' : '刷新打印机列表' }}
            </button>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="form-control">
              <label class="label"><span class="label-text font-medium">份数 <span class="text-error">*</span></span></label>
              <input v-model.number="form.copies" type="number" min="1" max="100" class="input input-bordered w-full" required />
            </div>
            <div class="form-control">
              <label class="label"><span class="label-text font-medium">任务名称</span></label>
              <input v-model="form.jobName" type="text" placeholder="可选，自动生成" class="input input-bordered w-full" />
            </div>
          </div>

          <div class="form-control">
            <div class="flex items-center justify-between">
              <label class="label-label"><span class="label-text font-medium">打印数据 <span class="text-error">*</span></span></label>
              <button type="button" class="text-xs text-primary cursor-pointer hover:underline" @click="useTemplateData">使用模板默认数据</button>
            </div>
            <JsonEditor ref="editorRef" v-model="form.printDataJson" mode="text" :indent="2" />
            <p class="text-xs text-base-content/40 mt-1">必须是 JSON 数组，每个元素对应一页数据</p>
          </div>

          <div class="flex gap-3 pt-2">
            <button type="button" class="btn btn-ghost" @click="handlePreview" :disabled="loading">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              预览 PDF
            </button>
            <button type="submit" class="btn btn-primary flex-1 gap-2" :disabled="submitting">
              <svg v-if="submitting" class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.219-8.56"/><path d="M21 3v6h-6"/></svg>
              <svg v-else class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 01-2-2v-5a2 2 0 012-2h16a2 2 0 012 2v5a2 2 0 01-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>
              {{ submitting ? '提交中...' : '提交打印' }}
            </button>
          </div>
        </form>
      </div>

      <!-- 提交结果 -->
      <div class="space-y-4">
        <div v-if="submittedJob" class="bg-success/10 border border-success/30 rounded-lg p-6">
          <div class="flex items-start gap-3">
            <svg class="w-6 h-6 text-success shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
            <div>
              <h3 class="font-semibold text-success">打印任务已提交</h3>
              <p class="text-sm text-base-content/60 mt-1">任务ID: #{{ submittedJob.cupsJobId }}</p>
              <p class="text-sm text-base-content/60">打印机: {{ submittedJob.printerName }}</p>
              <router-link to="/print-jobs" class="btn btn-sm btn-success mt-3 gap-2">
                查看打印任务
                <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
              </router-link>
            </div>
          </div>
        </div>

        <!-- PDF 预览 -->
        <div v-if="previewUrl" class="bg-white rounded-lg border border-sidebar-border overflow-hidden">
          <div class="flex items-center justify-between p-4 border-b border-sidebar-border">
            <h3 class="font-semibold text-base-content">PDF 预览</h3>
            <button class="btn btn-ghost btn-sm" @click="closePreview">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <iframe :src="previewUrl" class="w-full h-[600px] border-0"></iframe>
        </div>

        <div v-else class="bg-white rounded-lg border border-sidebar-border p-8">
          <div class="flex flex-col items-center gap-2 text-center">
            <svg class="w-12 h-12 text-base-content/20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
            </svg>
            <p class="text-base-content/40 text-sm">填写表单后点击"预览 PDF"查看渲染效果</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import JsonEditor from '../components/JsonEditor.vue'
import { cupsApi, type PrinterItem, type PrintJobItem } from '../api/cups'
import { printTemplateApi, type PrintTemplateItem } from '../api/printTemplate'
import { engineApi } from '../api/engine'
import { useToastStore } from '../stores/toast'
import { useRoute } from 'vue-router'
import { El } from 'element-plus'

const toast = useToastStore()
const route = useRoute()

const loading = ref(false)
const printersLoading = ref(false)
const templatesLoading = ref(false)
const submitting = ref(false)

const templates = ref<PrintTemplateItem[]>([])
const printers = ref<PrinterItem[]>([])
const editorRef = ref<InstanceType<typeof JsonEditor>>()
const previewUrl = ref('')
const submittedJob = ref<PrintJobItem | null>(null)

const form = ref({
  templateCode: '',
  printerName: '',
  copies: 1,
  jobName: '',
  printDataJson: '[]'
})

onMounted(async () => {
  await Promise.all([loadTemplates(), loadPrinters()])
  // 从 query 参数带入打印机
  if (route.query.printerName) {
    form.value.printerName = route.query.printerName as string
  }
})

onUnmounted(() => {
  closePreview()
})

async function loadTemplates() {
  templatesLoading.value = true
  try {
    // 获取全部模板（不分页）
    const res = await printTemplateApi.list({ page: 1, size: 100 })
    templates.value = res.data.content
  } catch { /* 拦截器已处理 */ } finally {
    templatesLoading.value = false
  }
}

async function loadPrinters() {
  printersLoading.value = true
  try {
    const res = await cupsApi.listPrinters()
    printers.value = res.data
  } catch { /* 拦截器已处理 */ } finally {
    printersLoading.value = false
  }
}

function useTemplateData() {
  const tpl = templates.value.find(t => t.code === form.value.templateCode)
  if (tpl?.printData) {
    try {
      // printData 是 JSON 字符串，格式化后放入 editor
      const parsed = JSON.parse(tpl.printData)
      form.value.printDataJson = JSON.stringify(parsed, null, 2)
    } catch {
      form.value.printDataJson = tpl.printData
    }
    toast.showSuccess('已加载模板默认数据')
  } else {
    toast.showError('当前模板没有默认打印数据')
  }
}

function parsePrintData(): Array<Record<string, unknown>> {
  try {
    const data = JSON.parse(form.value.printDataJson)
    if (!Array.isArray(data)) {
      throw new Error('打印数据必须是数组')
    }
    return data
  } catch (e: any) {
    throw new Error('打印数据 JSON 格式错误: ' + e.message)
  }
}

async function handlePreview() {
  if (!form.value.templateCode) {
    toast.showError('请先选择模板')
    return
  }
  let printData: Array<Record<string, unknown>>
  try {
    printData = parsePrintData()
  } catch (e: any) {
    toast.showError(e.message)
    return
  }

  submitting.value = true
  try {
    const blob = await engineApi.previewPdf({ code: form.value.templateCode, printData })
    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = URL.createObjectURL(blob)
  } catch { /* 拦截器已处理 */ } finally {
    submitting.value = false
  }
}

function closePreview() {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

async function handleSubmit() {
  if (!form.value.templateCode || !form.value.printerName) {
    toast.showError('请填写必填项')
    return
  }

  let printData: Array<Record<string, unknown>>
  try {
    printData = parsePrintData()
  } catch (e: any) {
    toast.showError(e.message)
    return
  }

  submitting.value = true
  try {
    const res = await cupsApi.submitJob({
      templateCode: form.value.templateCode,
      printerName: form.value.printerName,
      copies: form.value.copies,
      printData,
      jobName: form.value.jobName || undefined
    })
    submittedJob.value = res.data
    closePreview()
    toast.showSuccess('打印任务已提交')
  } catch { /* 拦截器已处理 */ } finally {
    submitting.value = false
  }
}
</script>
