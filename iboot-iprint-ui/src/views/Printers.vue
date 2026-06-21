<template>
  <div>
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-base-content">打印机</h1>
        <p class="text-sm text-base-content/50 mt-1">CUPS 打印机列表和状态</p>
      </div>
      <button class="btn btn-primary gap-2" :disabled="loading" @click="loadList">
        <svg class="w-4 h-4" :class="{ 'animate-spin': loading }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 12a9 9 0 11-6.219-8.56"/><path d="M21 3v6h-6"/>
        </svg>
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>

    <!-- 打印机列表 -->
    <div class="bg-white rounded-lg border border-sidebar-border overflow-hidden">
      <table class="table">
        <thead>
          <tr class="bg-base-200/50">
            <th class="font-semibold text-base-content/70">打印机名称</th>
            <th class="font-semibold text-base-content/70">描述</th>
            <th class="font-semibold text-base-content/70">位置</th>
            <th class="font-semibold text-base-content/70">状态</th>
            <th class="font-semibold text-base-content/70">队列任务</th>
            <th class="font-semibold text-base-content/70">默认</th>
            <th class="font-semibold text-base-content/70">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading && list.length === 0">
            <td colspan="7" class="text-center py-16">
              <span class="loading loading-spinner loading-md"></span>
            </td>
          </tr>
          <tr v-else-if="list.length === 0">
            <td colspan="7" class="text-center py-16">
              <div class="flex flex-col items-center gap-2">
                <svg class="w-12 h-12 text-base-content/20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 01-2-2v-5a2 2 0 012-2h16a2 2 0 012 2v5a2 2 0 01-2 2h-2"/><rect x="6" y="14" width="12" height="8"/>
                </svg>
                <p class="text-base-content/40 text-sm">暂无打印机</p>
              </div>
            </td>
          </tr>
          <tr v-for="item in list" :key="item.name" class="hover:bg-base-200/30 transition-colors duration-150">
            <td class="font-medium text-base-content">{{ item.name }}</td>
            <td class="text-sm text-base-content/60">{{ item.description || '-' }}</td>
            <td class="text-sm text-base-content/50">{{ item.location || '-' }}</td>
            <td>
              <span class="badge" :class="stateBadgeClass(item.state)">{{ stateLabel(item.state) }}</span>
            </td>
            <td class="text-sm text-base-content/60">{{ item.jobCount }}</td>
            <td>
              <span v-if="item.default" class="badge badge-primary badge-sm">默认</span>
              <span v-else class="text-base-content/30">-</span>
            </td>
            <td>
              <button class="btn btn-ghost btn-xs gap-1 cursor-pointer" @click="viewJobs(item)">查看任务</button>
              <button class="btn btn-ghost btn-xs gap-1 text-primary hover:bg-primary/10 cursor-pointer" @click="submitPrint(item)">提交打印</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { cupsApi, type PrinterItem } from '../api/cups'
import { useRouter } from 'vue-router'
import { useToastStore } from '../stores/toast'

const toast = useToastStore()
const router = useRouter()
const loading = ref(false)
const list = ref<PrinterItem[]>([])

onMounted(() => { loadList() })

async function loadList() {
  loading.value = true
  try {
    const res = await cupsApi.listPrinters()
    list.value = res.data
  } catch { /* 拦截器已处理 */ } finally {
    loading.value = false
  }
}

function stateLabel(state: string) {
  const map: Record<string, string> = { idle: '空闲', printing: '打印中', stopped: '已停止', UNKNOWN: '未知' }
  return map[state?.toLowerCase()] || state || '未知'
}

function stateBadgeClass(state: string) {
  const s = state?.toLowerCase()
  if (s === 'idle') return 'badge-success'
  if (s === 'printing') return 'badge-warning'
  if (s === 'stopped') return 'badge-error'
  return 'badge-neutral'
}

function viewJobs(item: PrinterItem) {
  router.push({ path: '/print-jobs', query: { printerName: item.name } })
}

function submitPrint(item: PrinterItem) {
  router.push({ path: '/print-submit', query: { printerName: item.name } })
}
</script>
