<template>
  <div>
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-base-content">概览</h1>
      <p class="text-sm text-base-content/50 mt-1">系统运行状态一览</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <div class="bg-white rounded-lg border border-sidebar-border p-5">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm text-base-content/50">打印模版</span>
          <div class="w-9 h-9 rounded-lg bg-primary/10 flex items-center justify-center">
            <svg class="w-4.5 h-4.5 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
        </div>
        <div class="text-3xl font-bold text-base-content">{{ stats.templateCount }}</div>
      </div>

      <div class="bg-white rounded-lg border border-sidebar-border p-5">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm text-base-content/50">API Key 总数</span>
          <div class="w-9 h-9 rounded-lg bg-blue-50 flex items-center justify-center">
            <svg class="w-4.5 h-4.5 text-blue-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
            </svg>
          </div>
        </div>
        <div class="text-3xl font-bold text-base-content">{{ stats.apiKeyCount }}</div>
      </div>

      <div class="bg-white rounded-lg border border-sidebar-border p-5">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm text-base-content/50">已启用 Key</span>
          <div class="w-9 h-9 rounded-lg bg-emerald-50 flex items-center justify-center">
            <svg class="w-4.5 h-4.5 text-emerald-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
        </div>
        <div class="text-3xl font-bold text-emerald-600">{{ stats.apiKeyActive }}</div>
      </div>

      <div class="bg-white rounded-lg border border-sidebar-border p-5">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm text-base-content/50">已禁用 Key</span>
          <div class="w-9 h-9 rounded-lg bg-red-50 flex items-center justify-center">
            <svg class="w-4.5 h-4.5 text-red-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>
        </div>
        <div class="text-3xl font-bold text-red-500">{{ stats.apiKeyDisabled }}</div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="mb-8">
      <h2 class="text-base font-semibold text-base-content mb-3">快捷入口</h2>
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <router-link to="/templates" class="bg-white rounded-lg border border-sidebar-border p-4 flex items-center gap-3 hover:border-primary/30 hover:shadow-sm transition-all cursor-pointer group">
          <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center group-hover:bg-primary/15 transition-colors">
            <svg class="w-5 h-5 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
          <div>
            <div class="text-sm font-medium text-base-content">打印模版</div>
            <div class="text-xs text-base-content/40">管理打印模版</div>
          </div>
        </router-link>

        <router-link to="/api-keys" class="bg-white rounded-lg border border-sidebar-border p-4 flex items-center gap-3 hover:border-primary/30 hover:shadow-sm transition-all cursor-pointer group">
          <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center group-hover:bg-blue-100 transition-colors">
            <svg class="w-5 h-5 text-blue-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
            </svg>
          </div>
          <div>
            <div class="text-sm font-medium text-base-content">API Key</div>
            <div class="text-xs text-base-content/40">管理访问密钥</div>
          </div>
        </router-link>

        <router-link to="/change-password" class="bg-white rounded-lg border border-sidebar-border p-4 flex items-center gap-3 hover:border-primary/30 hover:shadow-sm transition-all cursor-pointer group">
          <div class="w-10 h-10 rounded-lg bg-amber-50 flex items-center justify-center group-hover:bg-amber-100 transition-colors">
            <svg class="w-5 h-5 text-amber-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </div>
          <div>
            <div class="text-sm font-medium text-base-content">修改密码</div>
            <div class="text-xs text-base-content/40">更新登录密码</div>
          </div>
        </router-link>

        <a href="https://gitee.com/abc00/vue-plugin-hiprint?skip_mobile=true" target="_blank" rel="noopener" class="bg-white rounded-lg border border-sidebar-border p-4 flex items-center gap-3 hover:border-primary/30 hover:shadow-sm transition-all cursor-pointer group">
          <div class="w-10 h-10 rounded-lg bg-violet-50 flex items-center justify-center group-hover:bg-violet-100 transition-colors">
            <svg class="w-5 h-5 text-violet-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
            </svg>
          </div>
          <div>
            <div class="text-sm font-medium text-base-content">Hiprint 文档</div>
            <div class="text-xs text-base-content/40">模版设计器</div>
          </div>
        </a>
      </div>
    </div>

    <!-- API 端点参考 -->
    <div>
      <h2 class="text-base font-semibold text-base-content mb-3">API 端点</h2>
      <div class="bg-white rounded-lg border border-sidebar-border overflow-hidden">
        <table class="table text-sm">
          <thead>
            <tr class="bg-base-200/50">
              <th class="font-semibold text-base-content/70 w-24">方法</th>
              <th class="font-semibold text-base-content/70">端点</th>
              <th class="font-semibold text-base-content/70">说明</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="api in apiEndpoints" :key="api.path" class="hover:bg-base-200/30">
              <td><span class="badge badge-sm font-mono" :class="methodClass(api.method)">{{ api.method }}</span></td>
              <td><code class="text-xs bg-base-200 px-1.5 py-0.5 rounded font-mono">{{ api.path }}</code></td>
              <td class="text-base-content/60">{{ api.desc }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../api/index'

const stats = ref({ templateCount: 0, apiKeyCount: 0, apiKeyActive: 0, apiKeyDisabled: 0 })

onMounted(async () => {
  try {
    const res = await http.get<any, { code: number; data: Record<string, number> }>('/dashboard/stats')
    stats.value = res.data as any
  } catch { /* ignore */ }
})

const apiEndpoints = [
  { method: 'POST', path: '/api/engine/render/pdf', desc: '通过模版编码 + 打印数据生成 PDF' },
  { method: 'POST', path: '/api/engine/render/html', desc: '通过模版编码 + 打印数据生成 HTML' },
  { method: 'POST', path: '/api/engine/generatePdf', desc: '通过模版 JSON + 打印数据生成 PDF' },
  { method: 'POST', path: '/api/engine/generateHtml', desc: '通过模版 JSON + 打印数据生成 HTML' },
  { method: 'GET', path: '/api/engine/version', desc: '获取 Hiprint 版本号' },
]

function methodClass(method: string) {
  return method === 'POST' ? 'badge-primary' : 'badge-success'
}
</script>
