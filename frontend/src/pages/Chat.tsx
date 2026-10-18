import { useEffect, useRef, useState } from "react";
import type { FormEvent } from "react";
import { Send, Sparkles, Bot, User } from "lucide-react";
import { api } from "../api";
type Message = { role: "user" | "assistant"; content: string };
export default function Chat() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const end = useRef<HTMLDivElement>(null);
  useEffect(() => {
    end.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading, error]);
  async function send(e: FormEvent) {
    e.preventDefault();
    if (!input.trim() || loading) return;
    const message = input.trim();
    const history: Message[] = [
      ...messages,
      { role: "user", content: message },
    ];
    setMessages(history);
    setInput("");
    setLoading(true);
    setError("");
    try {
      const result = await api<{ response: string }>("/chat", "POST", {
        message,
        history: messages.slice(-20),
      });
      setMessages([
        ...history,
        { role: "assistant", content: result.response },
      ]);
    } catch (e) {
      setMessages(messages);
      setInput(message);
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  }
  return (
    <main className="chat-page">
      <div className="page-heading">
        <div>
          <span className="eyebrow">LOCAL AI ASSISTANT</span>
          <h1>AI Chat</h1>
          <p>Ollama üzerinde çalışan yerel modelinizle sohbet edin.</p>
        </div>
        <span className="badge">llama3.2:latest</span>
      </div>
      <section className="panel chat">
        <div className="chat-header">
          <Sparkles size={21} />
          <strong>Akademik asistan</strong>
          <button
            disabled={loading || !messages.length}
            onClick={() => {
              setMessages([]);
              setError("");
            }}
          >
            Sohbeti temizle
          </button>
        </div>
        <div className="messages" aria-live="polite" aria-busy={loading}>
          {!messages.length && (
            <div className="welcome">
              <span className="welcome-icon">
                <Bot size={38} />
              </span>
              <h2>Bugün ne öğrenmek istersiniz?</h2>
              <p>Dersler, yazılım ve büyük veri hakkında soru sorun.</p>
              <div className="suggestions">
                {[
                  "Hadoop HDFS nasıl çalışır?",
                  "Redis cache-aside yaklaşımını açıkla.",
                  "SQL JOIN için bir örnek ver.",
                ].map((s) => (
                  <button key={s} onClick={() => setInput(s)}>
                    {s}
                  </button>
                ))}
              </div>
            </div>
          )}
          {messages.map((m, i) => (
            <article className={`message ${m.role}`} key={i}>
              <span className="message-icon">
                {m.role === "user" ? <User size={18} /> : <Bot size={18} />}
              </span>
              <div>
                <small>{m.role === "user" ? "Siz" : "AI Assistant"}</small>
                <p>{m.content}</p>
              </div>
            </article>
          ))}
          {loading && (
            <div className="message">
              <Bot size={20} />
              <p role="status">Yanıt hazırlanıyor…</p>
            </div>
          )}
          {error && (
            <p role="alert" className="error">
              {error}
            </p>
          )}
          <div ref={end} />
        </div>
        <form className="composer" onSubmit={send}>
          <label className="sr-only" htmlFor="message">
            Mesajınız
          </label>
          <textarea
            id="message"
            placeholder="Bir mesaj yazın…"
            maxLength={8000}
            rows={2}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => {
              if (
                e.key === "Enter" &&
                !e.shiftKey &&
                !e.nativeEvent.isComposing
              ) {
                e.preventDefault();
                void send(e);
              }
            }}
          />
          <button
            className="primary"
            aria-label="Mesaj gönder"
            disabled={loading || !input.trim()}
          >
            <Send size={20} />
          </button>
        </form>
        <small className="chat-note">
          Yanıtlar yerel AI modeli tarafından üretilir; hata içerebilir.
        </small>
      </section>
    </main>
  );
}
