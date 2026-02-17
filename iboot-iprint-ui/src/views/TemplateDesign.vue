<template>
  <div class="h-screen flex flex-col bg-page-bg overflow-hidden relative">
    <!-- 顶部工具栏 -->
    <header class="h-12 bg-white border-b border-sidebar-border flex items-center justify-between px-4 shrink-0">
      <div class="flex items-center gap-3">
        <button class="btn btn-sm btn-ghost gap-1" @click="goBack">
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
          返回
        </button>
        <div class="w-px h-5 bg-sidebar-border"></div>
        <span class="text-sm font-medium text-base-content">{{ templateName }}</span>
      </div>
      <div class="flex items-center gap-2">
        <!-- 纸张设置 -->
        <select class="select select-bordered select-xs" v-model="paperSize" @change="changePaper">
          <option value="A3">A3</option>
          <option value="A4">A4</option>
          <option value="A5">A5</option>
          <option value="B4">B4</option>
          <option value="B5">B5</option>
          <option value="custom">自定义</option>
        </select>
        <template v-if="paperSize === 'custom'">
          <input type="number" v-model.number="customWidth" class="input input-bordered input-xs w-16 text-center" placeholder="宽" min="1" @change="applyCustomPaper" />
          <span class="text-xs text-base-content/50">x</span>
          <input type="number" v-model.number="customHeight" class="input input-bordered input-xs w-16 text-center" placeholder="高" min="1" @change="applyCustomPaper" />
          <span class="text-xs text-base-content/40">mm</span>
        </template>
        <button class="btn btn-xs btn-ghost" @click="rotatePaper" title="旋转纸张">
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg>
        </button>
        <div class="w-px h-5 bg-sidebar-border"></div>
        <!-- 缩放 -->
        <button class="btn btn-xs btn-ghost" @click="zoomOut" title="缩小">
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="8" y1="11" x2="14" y2="11"/></svg>
        </button>
        <span class="text-xs text-base-content/60 w-10 text-center">{{ Math.round(scaleValue * 100) }}%</span>
        <button class="btn btn-xs btn-ghost" @click="zoomIn" title="放大">
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="11" y1="8" x2="11" y2="14"/><line x1="8" y1="11" x2="14" y2="11"/></svg>
        </button>
        <div class="w-px h-5 bg-sidebar-border"></div>
        <!-- 清空 -->
        <button class="btn btn-xs btn-ghost" @click="clearDesign" title="清空画布">
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        </button>
        <div class="w-px h-5 bg-sidebar-border"></div>
        <!-- 预览 -->
        <button class="btn btn-xs btn-ghost gap-1" @click="showPreview" title="预览">
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
          预览
        </button>
        <div class="w-px h-5 bg-sidebar-border"></div>
        <button class="btn btn-sm btn-primary gap-1" @click="handleSave" :disabled="saving">
          <span v-if="saving" class="loading loading-spinner loading-xs"></span>
          <svg v-else class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
          保存
        </button>
      </div>
    </header>

    <!-- 主体三栏布局 -->
    <div class="flex flex-1 min-h-0" @mousemove="onResizeMove" @mouseup="onResizeEnd" @mouseleave="onResizeEnd">
      <!-- 左侧：拖拽元素面板 -->
      <div v-show="leftPanelOpen" class="bg-white overflow-y-auto shrink-0 p-3" :style="{ width: leftPanelWidth + 'px' }">
        <h3 class="text-xs font-semibold text-base-content/50 uppercase mb-3">拖拽元素</h3>
        <div class="grid grid-cols-2 gap-1.5">
          <a class="ep-draggable-item" tid="defaultModule.text">
            <span class="hiprint-icon">T</span>
            <span class="hiprint-label">文本</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.image">
            <span class="hiprint-icon">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
            </span>
            <span class="hiprint-label">图片</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.longText">
            <span class="hiprint-icon">P</span>
            <span class="hiprint-label">长文本</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.table">
            <span class="hiprint-icon">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="3" y1="15" x2="21" y2="15"/><line x1="9" y1="3" x2="9" y2="21"/></svg>
            </span>
            <span class="hiprint-label">表格</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.emptyTable">
            <span class="hiprint-icon">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="3" x2="9" y2="21"/></svg>
            </span>
            <span class="hiprint-label">空白表格</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.html">
            <span class="hiprint-icon">&lt;/&gt;</span>
            <span class="hiprint-label">HTML</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.customText">
            <span class="hiprint-icon">Tc</span>
            <span class="hiprint-label">自定义文本</span>
          </a>
          <div class="col-span-2 pt-2 mt-1 border-t border-sidebar-border">
            <h3 class="text-xs font-semibold text-base-content/50 uppercase mb-1">辅助元素</h3>
          </div>
          <a class="ep-draggable-item" tid="defaultModule.hline">
            <span class="hiprint-icon">&#8212;</span>
            <span class="hiprint-label">横线</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.vline">
            <span class="hiprint-icon">|</span>
            <span class="hiprint-label">竖线</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.rect">
            <span class="hiprint-icon">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/></svg>
            </span>
            <span class="hiprint-label">矩形</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.oval">
            <span class="hiprint-icon">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/></svg>
            </span>
            <span class="hiprint-label">椭圆</span>
          </a>
          <div class="col-span-2 pt-2 mt-1 border-t border-sidebar-border">
            <h3 class="text-xs font-semibold text-base-content/50 uppercase mb-1">条码</h3>
          </div>
          <a class="ep-draggable-item" tid="defaultModule.barcode">
            <span class="hiprint-icon">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="2" height="16"/><rect x="6" y="4" width="1" height="16"/><rect x="9" y="4" width="2" height="16"/><rect x="14" y="4" width="1" height="16"/><rect x="17" y="4" width="3" height="16"/><rect x="22" y="4" width="1" height="16"/></svg>
            </span>
            <span class="hiprint-label">条形码</span>
          </a>
          <a class="ep-draggable-item" tid="defaultModule.qrcode">
            <span class="hiprint-icon">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="3" height="3"/></svg>
            </span>
            <span class="hiprint-label">二维码</span>
          </a>
        </div>
      </div>

      <!-- 左侧分隔条：拖拽调整 + 收起展开 -->
      <div class="panel-resizer" @mousedown.prevent="startResize('left', $event)">
        <button class="panel-resizer-btn" @click.stop="leftPanelOpen = !leftPanelOpen" :title="leftPanelOpen ? '收起' : '展开'">
          <svg class="w-3 h-3 transition-transform" :class="{ 'rotate-180': !leftPanelOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
      </div>

      <!-- 中间：设计画布 -->
      <div class="flex-1 overflow-auto bg-base-200/30" id="hiprint-printTemplate-wrapper">
        <div v-if="loading" class="flex items-center justify-center h-full">
          <span class="loading loading-spinner loading-lg"></span>
          <span class="ml-3 text-base-content/50">加载中...</span>
        </div>
        <div id="hiprint-printTemplate" class="hiprint-printTemplate"></div>
      </div>

      <!-- 右侧分隔条：拖拽调整 + 收起展开 -->
      <div class="panel-resizer" @mousedown.prevent="startResize('right', $event)">
        <button class="panel-resizer-btn" @click.stop="rightPanelOpen = !rightPanelOpen" :title="rightPanelOpen ? '收起' : '展开'">
          <svg class="w-3 h-3 transition-transform" :class="{ 'rotate-180': !rightPanelOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
      </div>

      <!-- 右侧：属性设置面板 -->
      <div v-show="rightPanelOpen" class="bg-white overflow-y-auto shrink-0" :style="{ width: rightPanelWidth + 'px' }">
        <div class="p-3">
          <h3 class="text-xs font-semibold text-base-content/50 uppercase mb-3">元素属性</h3>
          <div id="PrintElementOptionSetting"></div>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <dialog ref="previewModalRef" class="modal">
      <div class="modal-box w-11/12 max-w-5xl h-[90vh] flex flex-col p-0">
        <div class="flex items-center justify-between px-6 py-3 border-b border-sidebar-border shrink-0">
          <h3 class="text-lg font-bold">打印预览</h3>
          <div class="flex items-center gap-2">
            <button class="btn btn-sm btn-ghost gap-1" @click="handlePrint">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>
              打印
            </button>
            <button class="btn btn-sm btn-ghost" @click="previewModalRef?.close()">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
        </div>
        <div class="flex-1 overflow-auto bg-base-200/30 p-6">
          <div id="preview-content" class="flex justify-center"></div>
        </div>
      </div>
      <form method="dialog" class="modal-backdrop"><button>close</button></form>
    </dialog>

    <!-- 页脚版权 -->
    <footer class="absolute bottom-1 left-0 right-0 text-center text-[10px] text-base-content/20 pointer-events-none">
      &copy; {{ new Date().getFullYear() }} iBoot. All rights reserved.
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { printTemplateApi, type PrintTemplateItem } from '../api/printTemplate'
import { useToastStore } from '../stores/toast'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const loading = ref(true)
const saving = ref(false)
const templateName = ref('')
const scaleValue = ref(1)
const paperSize = ref('A4')
const customWidth = ref(210)
const customHeight = ref(297)
const previewModalRef = ref<HTMLDialogElement>()
const leftPanelOpen = ref(true)
const rightPanelOpen = ref(true)

// 面板拖拽调整宽度
const PANEL_MIN_WIDTH = 160
const PANEL_MAX_WIDTH = 480
const leftPanelWidth = ref(PANEL_MAX_WIDTH)
const rightPanelWidth = ref(PANEL_MAX_WIDTH)
let resizing: 'left' | 'right' | null = null
let resizeStartX = 0
let resizeStartWidth = 0

function startResize(side: 'left' | 'right', e: MouseEvent) {
  resizing = side
  resizeStartX = e.clientX
  resizeStartWidth = side === 'left' ? leftPanelWidth.value : rightPanelWidth.value
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

function onResizeMove(e: MouseEvent) {
  if (!resizing) return
  const delta = e.clientX - resizeStartX
  let newWidth: number
  if (resizing === 'left') {
    newWidth = resizeStartWidth + delta
  } else {
    newWidth = resizeStartWidth - delta
  }
  newWidth = Math.max(PANEL_MIN_WIDTH, Math.min(PANEL_MAX_WIDTH, newWidth))
  if (resizing === 'left') {
    leftPanelWidth.value = newWidth
  } else {
    rightPanelWidth.value = newWidth
  }
}

function onResizeEnd() {
  if (!resizing) return
  resizing = null
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

let templateId: number
let templateItem: PrintTemplateItem | null = null
let hiprintTemplate: any = null
let hiprint: any = null
let defaultElementTypeProvider: any = null

const paperSizes: Record<string, { width: number; height: number }> = {
  A3: { width: 297, height: 420 },
  A4: { width: 210, height: 297 },
  A5: { width: 148, height: 210 },
  B4: { width: 250, height: 353 },
  B5: { width: 176, height: 250 }
}

onMounted(async () => {
  templateId = Number(route.params.id)
  if (!templateId) {
    toast.showError('模板 ID 无效')
    goBack()
    return
  }

  try {
    const res = await printTemplateApi.get(templateId)
    templateItem = res.data
    templateName.value = templateItem.name

    const module = await import('@anganing/vue-plugin-hiprint')
    hiprint = module.hiprint
    defaultElementTypeProvider = module.defaultElementTypeProvider

    await nextTick()
    initDesigner()
  } catch {
    toast.showError('加载模板失败')
    goBack()
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  if (hiprintTemplate) {
    hiprintTemplate = null
  }
})

function initDesigner() {
  hiprint.init({
    providers: [new defaultElementTypeProvider()]
  })
  hiprint.setConfig()
  hiprint.PrintElementTypeManager.buildByHtml(window.$('.ep-draggable-item'))

  let templateData = {}
  if (templateItem?.templateData) {
    try {
      templateData = JSON.parse(templateItem.templateData)
    } catch { /* use empty */ }
  }

  hiprintTemplate = new hiprint.PrintTemplate({
    template: templateData,
    settingContainer: '#PrintElementOptionSetting',
    paginationContainer: '.hiprint-printPagination',
    dataMode: 1,
    history: true,
    onDataChanged: () => {},
    fontList: [
      { title: '微软雅黑', value: 'Microsoft YaHei' },
      { title: '宋体', value: 'SimSun' },
      { title: '黑体', value: 'SimHei' },
      { title: '楷体', value: 'KaiTi' },
      { title: 'Arial', value: 'Arial' },
      { title: 'Times New Roman', value: 'Times New Roman' }
    ]
  })

  hiprintTemplate.design('#hiprint-printTemplate', { grid: true })
}

async function handleSave() {
  if (!hiprintTemplate || !templateItem) return
  saving.value = true
  try {
    const json = hiprintTemplate.getJson()
    const templateData = JSON.stringify(json)
    await printTemplateApi.update(templateId, {
      code: templateItem.code,
      name: templateItem.name,
      templateData,
      printData: templateItem.printData || ''
    })
    templateItem.templateData = templateData
    toast.showSuccess('保存成功')
  } catch { /* interceptor handled */ } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/templates')
}

function changePaper() {
  if (!hiprintTemplate) return
  if (paperSize.value === 'custom') return
  const size = paperSizes[paperSize.value]
  if (size) {
    hiprintTemplate.setPaper(size.width, size.height)
  }
}

function applyCustomPaper() {
  if (!hiprintTemplate) return
  if (customWidth.value > 0 && customHeight.value > 0) {
    hiprintTemplate.setPaper(customWidth.value, customHeight.value)
  }
}

function rotatePaper() {
  if (!hiprintTemplate) return
  hiprintTemplate.rotatePaper()
}

function zoomIn() {
  if (!hiprintTemplate) return
  scaleValue.value = Math.min(scaleValue.value + 0.1, 3)
  hiprintTemplate.zoom(scaleValue.value)
}

function zoomOut() {
  if (!hiprintTemplate) return
  scaleValue.value = Math.max(scaleValue.value - 0.1, 0.3)
  hiprintTemplate.zoom(scaleValue.value)
}

function clearDesign() {
  if (!hiprintTemplate) return
  hiprintTemplate.clear()
}

function getPrintData(): object {
  if (templateItem?.printData) {
    try {
      return JSON.parse(templateItem.printData)
    } catch { /* ignore */ }
  }
  return {}
}

function showPreview() {
  if (!hiprintTemplate) return
  const printData = getPrintData()
  previewModalRef.value?.showModal()
  nextTick(() => {
    window.$('#preview-content').html(hiprintTemplate.getHtml(printData))
  })
}

function handlePrint() {
  if (!hiprintTemplate) return
  const printData = getPrintData()
  hiprintTemplate.print(printData, {}, {
    callback: () => {}
  })
}


</script>

<style>
/* hiprint 设计器样式覆盖 */
.ep-draggable-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: move;
  text-decoration: none;
  color: #334155;
  font-size: 13px;
  transition: background-color 0.15s;
}

.ep-draggable-item:hover {
  background-color: #f1f5f9;
}

.ep-draggable-item .hiprint-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.ep-draggable-item .hiprint-label {
  white-space: nowrap;
}

/* 属性面板样式 */
#PrintElementOptionSetting {
  font-size: 12px;
}

#PrintElementOptionSetting .hiprint-option-item-label,
#PrintElementOptionSetting label {
  font-size: 12px;
  color: #64748b;
}

#PrintElementOptionSetting input,
#PrintElementOptionSetting select,
#PrintElementOptionSetting textarea {
  font-size: 12px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  padding: 4px 8px;
}

#PrintElementOptionSetting .hiprint-option-item {
  margin-bottom: 6px;
}

#PrintElementOptionSetting h3,
#PrintElementOptionSetting h4,
#PrintElementOptionSetting h5,
#PrintElementOptionSetting .title,
#PrintElementOptionSetting span,
#PrintElementOptionSetting div {
  font-size: 12px;
}

/* 画布容器 */
#hiprint-printTemplate {
  padding: 20px;
}

.hiprint-printTemplate .hiprint-printPaper {
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
  margin: 0 auto;
}

/* 预览内容样式 */
#preview-content .hiprint-printPaper {
  background: white;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
  margin-bottom: 16px;
}

/* 面板分隔条 */
.panel-resizer {
  position: relative;
  flex-shrink: 0;
  width: 6px;
  cursor: col-resize;
  background: #f1f5f9;
  border-left: 1px solid #e2e8f0;
  border-right: 1px solid #e2e8f0;
  transition: background-color 0.15s;
}

.panel-resizer:hover {
  background: #dbeafe;
}

.panel-resizer-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  cursor: pointer;
  color: #94a3b8;
  z-index: 1;
  transition: color 0.15s, border-color 0.15s;
}

.panel-resizer-btn:hover {
  color: #3b82f6;
  border-color: #93c5fd;
}
</style>
