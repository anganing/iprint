/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'jquery'
declare module '@anganing/vue-plugin-hiprint'

interface Window {
  jQuery: any
  $: any
}
