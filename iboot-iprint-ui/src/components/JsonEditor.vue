<template>
  <div ref="containerRef" class="json-editor-wrapper"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { JSONEditor, Mode, type Content, type OnChange } from 'vanilla-jsoneditor'

const props = defineProps<{
  modelValue: string
  readOnly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const containerRef = ref<HTMLDivElement>()
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let editor: any = null
let internalUpdate = false

function parseContent(str: string): Content {
  if (!str.trim()) return { text: '' }
  return { text: str }
}

onMounted(() => {
  if (!containerRef.value) return
  // @ts-expect-error vanilla-jsoneditor factory function
  editor = new JSONEditor({
    target: containerRef.value,
    props: {
      content: parseContent(props.modelValue),
      mode: Mode.text,
      readOnly: props.readOnly ?? false,
      mainMenuBar: true,
      navigationBar: false,
      statusBar: true,
      tabSize: 2,
      onChange: ((content: Content) => {
        internalUpdate = true
        if ('json' in content && content.json !== undefined) {
          emit('update:modelValue', JSON.stringify(content.json, null, 2))
        } else if ('text' in content && content.text !== undefined) {
          emit('update:modelValue', content.text)
        }
        internalUpdate = false
      }) as OnChange
    }
  })
})

watch(() => props.modelValue, (val) => {
  if (internalUpdate || !editor) return
  editor.update(parseContent(val))
})

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
    editor = null
  }
})
</script>

<style>
.json-editor-wrapper {
  --jse-theme-color: #2563eb;
  --jse-theme-color-highlight: #dbeafe;
  --jse-panel-background: #f8fafc;
  --jse-background-color: #ffffff;
  --jse-text-color: #1e293b;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.json-editor-wrapper .jse-text-mode {
  border: none !important;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.json-editor-wrapper .jse-text-mode .cm-editor {
  flex: 1;
  min-height: 0;
  overflow: auto;
}
.json-editor-wrapper .jse-status-bar {
  border-top: 1px solid #e2e8f0;
  font-size: 12px;
}
.json-editor-wrapper .jse-menu {
  border-bottom: 1px solid #e2e8f0;
}
</style>
