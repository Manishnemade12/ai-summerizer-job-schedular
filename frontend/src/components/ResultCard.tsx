import { Hash, Clock, CheckCircle2, Zap } from 'lucide-react';
import type { JobStatus } from '../services/api';

interface ResultCardProps {
  job: JobStatus;
  cached?: boolean;
}

export function ResultCard({ job, cached }: ResultCardProps) {
  if (!job.summary) return null;

  return (
    <div className="result-content">
      <h2 className="card-title">
        <Zap className="text-violet" size={20} />
        Generated Summary
      </h2>

      <p className="summary-text">{job.summary}</p>

      {job.tags && job.tags.length > 0 && (
        <div className="tag-cloud">
          {job.tags.map((tag, i) => (
            <span key={i} className="premium-tag">
              <Hash size={12} /> {tag}
            </span>
          ))}
        </div>
      )}

      <div className="meta-footer">
        {job.duration_ms !== undefined && (
          <span className="meta-item">
            <Clock size={14} /> {job.duration_ms}ms processing
          </span>
        )}
        {cached && (
          <span className="meta-item success">
            <CheckCircle2 size={14} /> Instant hit from Valkey
          </span>
        )}
      </div>
    </div>
  );
}
