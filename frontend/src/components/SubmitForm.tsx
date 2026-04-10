import { useState } from 'react';
import { Link2, Type, Zap } from 'lucide-react';

interface SubmitFormProps {
  onSubmit: (input: string, isUrl: boolean) => void;
  loading: boolean;
}

export function SubmitForm({ onSubmit, loading }: SubmitFormProps) {
  const [input, setInput] = useState('');
  const [mode, setMode] = useState<'text' | 'url'>('text');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;
    onSubmit(input.trim(), mode === 'url');
  };

  const placeholder =
    mode === 'text'
      ? 'Paste your article, document, or thought here. Let AI do the heavy lifting...'
      : 'https://example.com/article';

  return (
    <form className="submit-form" onSubmit={handleSubmit}>
      
      <div className="input-toggle-container">
        <button
          type="button"
          className={`toggle-pill ${mode === 'text' ? 'active' : ''}`}
          onClick={() => { setMode('text'); setInput(''); }}
        >
          <Type size={16} /> Text
        </button>
        <button
          type="button"
          className={`toggle-pill ${mode === 'url' ? 'active' : ''}`}
          onClick={() => { setMode('url'); setInput(''); }}
        >
          <Link2 size={16} /> URL
        </button>
      </div>

      <div className="hero-input-area">
        {mode === 'text' ? (
          <textarea
            className="hero-input"
            placeholder={placeholder}
            value={input}
            onChange={e => setInput(e.target.value)}
            disabled={loading}
          />
        ) : (
          <input
            type="url"
            className="hero-input"
            placeholder={placeholder}
            value={input}
            onChange={e => setInput(e.target.value)}
            disabled={loading}
            autoComplete="off"
          />
        )}
      </div>

      <button
        type="submit"
        className="hero-submit-btn"
        disabled={loading || !input.trim()}
      >
        {loading ? (
          <>
            <div className="spinner-ring" />
            Generating...
          </>
        ) : (
          <>
            <Zap size={18} fill="currentColor" />
            Summarize Now
          </>
        )}
      </button>
    </form>
  );
}
