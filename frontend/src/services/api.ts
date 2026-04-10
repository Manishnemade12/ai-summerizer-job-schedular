const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export interface SubmitResponse {
  job_id: string;
  status: string;
  cached: boolean;
  summary?: string;
  tags?: string[];
}

export interface JobStatus {
  job_id: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  summary?: string;
  tags?: string[];
  error?: string;
  duration_ms?: number;
  created_at?: string;
  completed_at?: string;
}

export interface Analytics {
  total_requests: number;
  cache_hits: number;
  cache_misses: number;
  failed_jobs: number;
  queue_size: number;
  avg_processing_time_ms: number;
}

export async function submitJob(input: string): Promise<SubmitResponse> {
  const res = await fetch(`${API_BASE}/submit`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ input }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || 'Failed to submit job');
  }
  return res.json();
}

export async function getJobStatus(jobId: string): Promise<JobStatus> {
  const res = await fetch(`${API_BASE}/status/${jobId}`);
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || 'Failed to fetch job status');
  }
  return res.json();
}

export async function getAnalytics(): Promise<Analytics> {
  const res = await fetch(`${API_BASE}/analytics`);
  if (!res.ok) throw new Error('Failed to fetch analytics');
  return res.json();
}
