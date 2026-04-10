import { useState, useCallback } from 'react';
import { Bot, AlertTriangle } from 'lucide-react';
import { SubmitForm } from '../components/SubmitForm';
import { JobStatusCard } from '../components/JobStatus';
import { ResultCard } from '../components/ResultCard';
import { submitJob, type SubmitResponse, type JobStatus } from '../services/api';

interface ActiveJob {
  id: string;
  status: string;
  cached: boolean;
  result?: JobStatus;
}

export function HomePage() {
  const [loading, setLoading] = useState(false);
  const [activeJob, setActiveJob] = useState<ActiveJob | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (input: string) => {
    setLoading(true);
    setError(null);
    setActiveJob(null);

    try {
      const resp: SubmitResponse = await submitJob(input);

      if (resp.status === 'completed' && resp.cached) {
        // Instant cache hit
        setActiveJob({
          id: resp.job_id,
          status: 'completed',
          cached: true,
          result: {
            job_id: resp.job_id,
            status: 'completed',
            summary: resp.summary,
            tags: resp.tags,
          },
        });
      } else {
        setActiveJob({ id: resp.job_id, status: resp.status, cached: false });
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Our servers are experiencing an issue. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleJobComplete = useCallback((job: JobStatus) => {
    setActiveJob(prev =>
      prev ? { ...prev, status: job.status, result: job } : null
    );
  }, []);

  return (
    <>
      <h1 className="page-title">Summarize Intelligence</h1>
      <p className="page-subtitle" style={{ marginBottom: 40 }}>
        Powered by Gemini AI. Input large texts or articles, process asynchronously, and get instant summaries via Valkey cache.
      </p>

      {error && (
        <div className="error-box" style={{ marginBottom: 32 }}>
          <AlertTriangle size={20} /> {error}
        </div>
      )}

      <div className="hero-split">
        {/* Left column: Input */}
        <div className="glass-card">
          <SubmitForm onSubmit={handleSubmit} loading={loading} />
        </div>

        {/* Right column: Results/Status */}
        <div className="glass-card" style={{ padding: activeJob ? '32px' : '48px 32px' }}>
          {!activeJob ? (
            <div className="empty-state">
              <div style={{ display: 'inline-flex', padding: 20, background: 'rgba(255,255,255,0.03)', borderRadius: '50%', marginBottom: 16 }}>
                <Bot size={48} className="text-violet" />
              </div>
              <h3 className="empty-title">Awaiting Input</h3>
              <p className="empty-desc" style={{ maxWidth: 300, margin: '0 auto' }}>
                Submit a URL or paste text to generate an AI-powered summary and insights.
              </p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 24, minHeight: '100%' }}>
              
              {activeJob.status !== 'completed' && activeJob.status !== 'failed' && (
                <JobStatusCard
                  jobId={activeJob.id}
                  initialStatus={activeJob.status}
                  onComplete={handleJobComplete}
                />
              )}

              {activeJob.result && (
                <ResultCard job={activeJob.result} cached={activeJob.cached} />
              )}

              {activeJob.status === 'failed' && (
                <div className="error-box mt-4">
                  <AlertTriangle size={20} /> Job failed: {activeJob.result?.error || 'Unknown error occurred.'}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </>
  );
}
