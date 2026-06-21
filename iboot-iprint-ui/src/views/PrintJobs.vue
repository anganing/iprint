<template>
  <div>
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-base-content">打印任务</h1>
        <p class="text-sm text-base-content/50 mt-1">查看 CUPS 打印任务队列</p>
      </div>
      <div class="flex gap-2">
        <button class="btn btn-ghost gap-2" :disabled="loading" @click="loadList">
          <svg class="w-4 h-4" :class="{ 'animate-spin': loading }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 12a9 9 0 11-6.219-8.56"/><path d="M21 3v6h-6"/>
          </svg>
          刷新
        </button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white rounded-lg border border-sidebar-border p-4 mb-4">
      <div class="flex items-center gap-3 flex-wrap">
        <div class="relative max-w-xs">
          <input v-model="query.printerName" type="text" placeholder="打印机名称" class="input input-bordered input-sm w-48" />
        </div>
        <select v-model="query.status" class="select select-bordered select-sm w-36">
          <option value="">全部状态</option>
          <option value="PENDING">等待中</option>
          <option value="PROCESSING">处理中</option>
          <option value="COMPLETED">已完成</option>
          <option value="ABORTED">已取消</option>
        </select>
        <button class="btn btn-sm btn-primary" @click="handleSearch">查询</button>
        <button class="btn btn-sm btn-ghost" @click="clearSearch">清除</button>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="bg-white rounded-lg border border-sidebar-border overflow-hidden">
      <table class="table">
        <thead>
          <tr class="bg-base-200/50">
            <th class="font-semibold text-base-content/70">任务ID</th>
            <th class="font-semibold text-base-content/70">任务名称</th>
            <th class="font-semibold text-base-content/70">打印机</th>
            <th class="font-semibold text-base-content/70">提交者</th>
            <th class="font-semibold text-base-content/70">状态</th>
            <th class="font-semibold text-base-content/70">已打印页数</th>
            <th class="font-semibold text-base-content/70">大小</th>
            <th class="font-semibold text-base-content/70">创建时间</th>
            <th class="font-semibold text-base-content/70">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading && pageData.content.length === 0">
            <td colspan="9" class="text-center py-16"><span class="loading loading-spinner loading-md"></span></td>
          </tr>
          <tr v-else-if="pageData.content.length === 0">
            <td colspan="9" class="text-center py-16">
              <div class="flex flex-col items-center gap-2">
                <svg class="w-12 h-12 text-base-content/20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="6" y="2" width="12" height="8" rx="1"/><path d="M6 14H4a2 2 0 01-2-2V6a2 2 0 012-2h16a2 2 0 012 2v6a2 2 0 01-2 2h-2"/><rect x="6" y="14" width="12" height="8"/>
                </svg>
                <p class="text-base-content/40 text-sm">暂无打印任务</p>
              </div>
            </td>
          </tr>
          <tr v-for="item in pageData.content" :key="item.cupsJobId" class="hover:bg-base-200/30 transition-colors duration-150">
            <td class="text-base-content/50 font-mono text-sm">#{{ item.cupsJobId }}</td>
            <td class="font-medium text-base-content">{{ item.jobName || '-' }}</td>
            <td class="text-sm text-base-content/60">{{ item.printerName }}</td>
            <td class="text-sm text-base-content/60">{{ item.userName || '-' }}</td>
            <td>
              <span class="badge" :class="stateBadgeClass(item.state)">{{ stateLabel(item.state) }}</span>
            </td>
            <td class="text-sm text-base-content/60">{{ item.jobMediaSheetsCompleted ?? '-' }}</td>
            <td class="text-sm text-base-content/50">{{ item.copies }}份</td>
            <td class="text-sm text-base-content/50">{{ item.timeAtCreation || '-' }}</td>
            <td>
              <button
                v-if="item.state !== 'COMPLETED' && item.state !== 'ABORTED' && item.state !== 'CANCELED'"
                class="btn btn-ghost btn-xs gap-1 text-error hover:bg-error/10 cursor-pointer"
                @click="handleCancel(item)"
              >取消</button>
              <span v-else class="text-base-content/30 text-xs">-</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { cupsApi, type PrintJobItem, type PageData } from '../api/cups'
import { useToastStore } from '../stores/toast'
import { useRoute } from 'vue-router'

const toast = useToastStore()
const route = useRoute()
const loading = ref(false)
const pageData = ref<PageData<PrintJobItem>>({ content: [], totalElements: 0, totalPages: 0, page: 1, size: 10 })
const currentPage = ref(1)

const query = ref({
  printerName: (route.query.printerName as string) || '',
  status: ''
})

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
  loading.value = true
  try {
    const res = await cupsApi.listJobs({
      page: currentPage.value,
      size: 10,
      printerName: query.value.printerName || undefined,
      status: query.value.status || undefined
    })
    pageData.value = res.data
  } catch { /* 拦截器已处理 */ } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadList()
}

function clearSearch() {
  query.value = { printerName: '', status: '' }
  currentPage.value = 1
  loadList()
}

function goPage(page: number) {
  currentPage.value = page
  loadList()
}

async function handleCancel(item: PrintJobItem) {
  if (!confirm(`确定要取消任务 #${item.cupsJobId} 吗？`)) return
  try {
    await cupsApi.cancelJob(item.printerName, item.cupsJobId)
    toast.showSuccess('任务已取消')
    await loadList()
  } catch { /* 拦截器已处理 */ }
}

function stateLabel(state: string) {
  const map: Record<string, string> = {
    pending: '等待中', pending_held: '已暂停', processing: '处理中',
    stopped: '已停止', completed: '已完成', canceled: '已取消', aborted: '已终止'
  }
  return map[state?.toLowerCase()] || state || '未知'
}

function stateBadgeClass(state: string) {
  const s = state?.toLowerCase()
  if (s === 'completed') return 'badge-success'
  if (s === 'processing') return 'badge-warning'
  if (s === 'aborted' || s === 'canceled' || s === 'stopped') return 'badge-error'
  if (s === 'pending_held') return 'badge-neutral'
  return 'badge-info'
}
</script>
