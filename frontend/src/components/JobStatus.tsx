import { useEffect, useState } from 'react';
import { Loader2, CheckCircle2, XCircle, Clock } from 'lucide-react';
import { getJobStatus, type JobStatus } from '../services/api';

interface JobStatusProps {
  jobId: string;
  initialStatus: string;
  onComplete: (job: JobStatus) => void;
}

export function JobStatusCard({ jobId, initialStatus, onComplete }: JobStatusProps) {
  const [status, setStatus] = useState(initialStatus);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (status === 'completed' || status === 'failed') return;

    const poll = async () => {
      try {
        const job = await getJobStatus(jobId);
        setStatus(job.status);
        if (job.status === 'completed' || job.status === 'failed') {
          onComplete(job);
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Polling error');
      }
    };

    const interval = setInterval(poll, 1500); // Poll slightly faster for UI responsiveness
    poll();
    return () => clearInterval(interval);
  }, [jobId, status, onComplete]);

  return (
    <div className="status-frame">
      <div className="status-header">
        <span className="pulse-badge">
          {status === 'pending' && <Clock size={16} />}
          {status === 'processing' && <Loader2 size={16} className="spinner-ring" style={{border: 'none', animation: 'spin 1s linear infinite'}}/>}
          {(status === 'completed') && <CheckCircle2 size={16} />}
          {(status === 'failed') && <XCircle size={16} />}
          <span style={{textTransform: 'capitalize'}}>{status}</span>
        </span>
      </div>

      {(status === 'pending' || status === 'processing') && (
        <div style={{ marginTop: 16 }}>
          <div className="fluid-progress">
            <div className="fluid-progress-bar" />
          </div>
          
          <div style={{ marginTop: 24 }}>
            <div className="skeleton-text" style={{ width: '100%' }} />
            <div className="skeleton-text" style={{ width: '90%' }} />
            <div className="skeleton-text" style={{ width: '60%' }} />
          </div>
          <p style={{ marginTop: 16, fontSize: 13, color: 'var(--accent-violet)', textAlign: 'center' }}>
            {status === 'pending' ? 'Job queued — Waiting for a worker...' : 'AI is processing and generating insights...'}
          </p>
        </div>
      )}

      {error && (
        <div className="error-box mt-4">
          <XCircle size={18} /> {error}
        </div>
      )}
    </div>
  );
}
