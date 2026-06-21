import http from './index'

export interface PrinterItem {
  name: string
  description: string
  location: string
  state: string
  default: boolean
  acceptingJobs: boolean
  jobCount: number
}

export interface PrintJobItem {
  cupsJobId: number
  printerName: string
  jobName: string
  userName: string
  state: string
  copies: number
  jobMediaSheetsCompleted: number
  timeAtCreation: string | null
  timeAtProcessing: string | null
  timeAtCompleted: string | null
}

export interface PageData<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface SubmitJobParams {
  templateCode: string
  printerName: string
  copies: number
  printData: Array<Record<string, unknown>>
  jobName?: string
}

export const cupsApi = {
  listPrinters: () => http.get<any, { code: number; message: string; data: PrinterItem[] }>('/printers'),

  getPrinterStatus: (name: string) =>
    http.get<any, { code: number; message: string; data: PrinterItem }>(`/printers/${encodeURIComponent(name)}`),

  submitJob: (data: SubmitJobParams) =>
    http.post<any, { code: number; message: string; data: PrintJobItem }>('/print/jobs', data),

  listJobs: (params: { page?: number; size?: number; printerName?: string; status?: string }) =>
    http.get<any, { code: number; message: string; data: PageData<PrintJobItem> }>('/print/jobs', { params }),

  getJob: (printerName: string, cupsJobId: number) =>
    http.get<any, { code: number; message: string; data: PrintJobItem }>(`/print/jobs/${cupsJobId}`, {
      params: { printerName }
    }),

  cancelJob: (printerName: string, cupsJobId: number) =>
    http.post<any, { code: number; message: string; data: void }>(`/print/jobs/${cupsJobId}/cancel`, null, {
      params: { printerName }
    })
}
