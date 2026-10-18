import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import {
  Plus,
  Pencil,
  Trash2,
  Camera,
  RefreshCw,
  Search,
  Users,
  BookOpen,
  CalendarDays,
} from "lucide-react";
import { api, assignmentPath } from "../api";
import type { Professor, Course, Assignment } from "../api";
import Modal from "../components/Modal";
type Tab = "professors" | "courses" | "assignments";
type Row = Professor | Course | Assignment;
type Editor = { kind: "edit" | "delete" | "photo"; row?: Row; tab: Tab };
const names = {
  professors: "Professors",
  courses: "Courses",
  assignments: "Teaching Assignments",
};
function Photo({
  id,
  path,
  name,
}: {
  id: number;
  path: string | null;
  name: string;
}) {
  const [failed, setFailed] = useState(false);
  return path && !failed ? (
    <img
      className="avatar"
      src={`/api/professors/${id}/photo?v=${encodeURIComponent(path)}`}
      alt={name}
      onError={() => setFailed(true)}
    />
  ) : (
    <span className="avatar placeholder" aria-label="Fotoğraf yok">
      {name.slice(0, 1).toUpperCase()}
    </span>
  );
}
export default function Management() {
  const [tab, setTab] = useState<Tab>("professors");
  const [professors, setProfessors] = useState<Professor[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [search, setSearch] = useState("");
  const [editor, setEditor] = useState<Editor | null>(null);
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState("");
  async function load() {
    setLoading(true);
    setError("");
    try {
      const [p, c, a] = await Promise.all([
        api<Professor[]>("/professors"),
        api<Course[]>("/courses"),
        api<Assignment[]>("/assignments"),
      ]);
      setProfessors(p);
      setCourses(c);
      setAssignments(a);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    let active = true;
    Promise.all([
      api<Professor[]>("/professors"),
      api<Course[]>("/courses"),
      api<Assignment[]>("/assignments"),
    ])
      .then(([p, c, a]) => {
        if (active) {
          setProfessors(p);
          setCourses(c);
          setAssignments(a);
        }
      })
      .catch((e: Error) => {
        if (active) setError(e.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);
  function open(kind: Editor["kind"], row?: Row) {
    setFormError("");
    setEditor({ kind, row, tab });
  }
  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!editor) return;
    const data = new FormData(e.currentTarget);
    const row = editor.row;
    const path =
      editor.tab === "assignments" && row
        ? assignmentPath(row as Assignment)
        : `/${editor.tab}${row ? `/${(row as Professor | Course).id}` : ""}`;
    setBusy(true);
    setFormError("");
    setNotice("");
    try {
      if (editor.kind === "delete") await api(path, "DELETE");
      else if (editor.kind === "photo") {
        const file = data.get("file") as File;
        if (
          !["image/jpeg", "image/png"].includes(file.type) ||
          file.size > 5 * 1024 * 1024
        )
          throw new Error("En fazla 5 MB boyutunda JPEG veya PNG seçin.");
        await api(`${path}/photo`, "POST", data);
      } else {
        let body: unknown;
        if (editor.tab === "professors")
          body = {
            name: String(data.get("name")).trim(),
            department: String(data.get("department")).trim(),
          };
        else if (editor.tab === "courses")
          body = {
            name: String(data.get("name")).trim(),
            credit: Number(data.get("credit")),
          };
        else
          body = {
            ...(!row
              ? {
                  professorId: Number(data.get("professorId")),
                  courseId: Number(data.get("courseId")),
                  startDate: data.get("startDate"),
                }
              : {}),
            studentCount: Number(data.get("studentCount")),
            endingDate: data.get("endingDate") || null,
          };
        const staged = data.get("staged") === "on";
        await api(path + (staged ? "/staged" : ""), row ? "PUT" : "POST", body);
        if (staged)
          setNotice(
            "Güncelleme Redis kuyruğuna alındı. Yaklaşık 15 saniye sonra MySQL’e aktarılır; Yenile ile kontrol edebilirsiniz.",
          );
      }
      setEditor(null);
      await load();
    } catch (e) {
      setFormError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }
  const rows: Row[] = (
    tab === "professors"
      ? professors
      : tab === "courses"
        ? courses
        : assignments
  ).filter((row) =>
    Object.values(row).some((value) =>
      String(value ?? "")
        .toLocaleLowerCase("tr")
        .includes(search.toLocaleLowerCase("tr")),
    ),
  );
  const row = editor?.row;
  return (
    <main>
      <div className="page-heading">
        <div>
          <span className="eyebrow">CAMPUS WORKSPACE</span>
          <h1>School Management</h1>
          <p>
            Akademik kadro, dersler ve öğretim atamalarını tek yerden yönetin.
          </p>
        </div>
        <button onClick={() => void load()} disabled={loading}>
          <RefreshCw size={17} className={loading ? "spin" : ""} /> Yenile
        </button>
      </div>
      <div className="stats">
        {(
          [
            [Users, "Professors", professors.length],
            [BookOpen, "Courses", courses.length],
            [CalendarDays, "Teaching Assignments", assignments.length],
          ] as const
        ).map(([Icon, title, count]) => (
          <div className="stat" key={title}>
            <span className="stat-icon">
              <Icon size={23} />
            </span>
            <div>
              <small>{title}</small>
              <strong>{loading || error ? "—" : count}</strong>
            </div>
          </div>
        ))}
      </div>
      {notice && (
        <p role="status" className="notice">
          {notice}
        </p>
      )}
      {error && (
        <p role="alert" className="error">
          {error} <button onClick={() => void load()}>Tekrar dene</button>
        </p>
      )}
      <section className="panel">
        <div className="tabs" role="tablist" aria-label="Kayıt türü">
          {(Object.keys(names) as Tab[]).map((t) => (
            <button
              key={t}
              role="tab"
              aria-selected={tab === t}
              aria-controls="records"
              id={`tab-${t}`}
              className={tab === t ? "selected" : ""}
              onClick={() => {
                setTab(t);
                setSearch("");
              }}
            >
              {names[t]}
            </button>
          ))}
        </div>
        <div className="toolbar">
          <div>
            <h2>{names[tab]}</h2>
            <p>{rows.length} kayıt</p>
          </div>
          <div className="tools">
            <label className="search">
              <Search size={18} />
              <input
                aria-label="Kayıtlarda ara"
                placeholder="Kayıtlarda ara…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </label>
            <button
              className="primary"
              disabled={loading || !!error}
              onClick={() => open("edit")}
            >
              <Plus size={18} /> Yeni kayıt
            </button>
          </div>
        </div>
        <div
          className="table-scroll"
          id="records"
          role="tabpanel"
          aria-labelledby={`tab-${tab}`}
        >
          <table>
            <thead>
              <tr>
                {(tab === "professors"
                  ? ["ID", "Photo", "Professor Name", "Department", "Actions"]
                  : tab === "courses"
                    ? ["ID", "Course Name", "Credit", "Actions"]
                    : [
                        "Photo",
                        "Professor Name",
                        "Department",
                        "Course Name",
                        "Credit",
                        "Student Count",
                        "Start Date",
                        "Ending Date",
                        "Actions",
                      ]
                ).map((h) => (
                  <th key={h} scope="col">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={9} className="empty">
                    Kayıtlar yükleniyor…
                  </td>
                </tr>
              ) : error ? (
                <tr>
                  <td colSpan={9} className="empty">
                    Veriler alınamadı.
                  </td>
                </tr>
              ) : rows.length === 0 ? (
                <tr>
                  <td colSpan={9} className="empty">
                    {search
                      ? "Aramanızla eşleşen kayıt yok."
                      : "Henüz kayıt yok. Yeni bir kayıt ekleyin."}
                  </td>
                </tr>
              ) : (
                rows.map((r) => {
                  const p = r as Professor;
                  const c = r as Course;
                  const a = r as Assignment;
                  return (
                    <tr key={tab === "assignments" ? assignmentPath(a) : p.id}>
                      {tab === "professors" ? (
                        <>
                          <td className="muted">#{p.id}</td>
                          <td>
                            <Photo
                              key={p.photoPath}
                              id={p.id}
                              path={p.photoPath}
                              name={p.name}
                            />
                          </td>
                          <td className="name">{p.name}</td>
                          <td>
                            <span className="badge">{p.department}</span>
                          </td>
                        </>
                      ) : tab === "courses" ? (
                        <>
                          <td className="muted">#{c.id}</td>
                          <td className="name">{c.name}</td>
                          <td>
                            <span className="badge">{c.credit} kredi</span>
                          </td>
                        </>
                      ) : (
                        <>
                          <td>
                            <Photo
                              key={a.photoPath}
                              id={a.professorId}
                              path={a.photoPath}
                              name={a.professorName}
                            />
                          </td>
                          <td className="name">{a.professorName}</td>
                          <td>{a.department}</td>
                          <td>{a.courseName}</td>
                          <td>{a.credit}</td>
                          <td>{a.studentCount}</td>
                          <td>{a.startDate}</td>
                          <td>{a.endingDate || "—"}</td>
                        </>
                      )}
                      <td>
                        <div className="actions">
                          <button
                            className="icon"
                            title="Düzenle"
                            aria-label="Düzenle"
                            onClick={() => open("edit", r)}
                          >
                            <Pencil size={17} />
                          </button>
                          {tab === "professors" && (
                            <button
                              className="icon"
                              title="Fotoğraf yükle"
                              aria-label="Fotoğraf yükle"
                              onClick={() => open("photo", r)}
                            >
                              <Camera size={17} />
                            </button>
                          )}
                          <button
                            className="icon danger"
                            title="Sil"
                            aria-label="Sil"
                            onClick={() => open("delete", r)}
                          >
                            <Trash2 size={17} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </section>
      {editor && (
        <Modal
          title={
            editor.kind === "delete"
              ? "Kaydı sil"
              : editor.kind === "photo"
                ? "Profesör fotoğrafı"
                : row
                  ? "Kaydı düzenle"
                  : "Yeni kayıt"
          }
          onClose={() => setEditor(null)}
          busy={busy}
        >
          <form onSubmit={submit}>
            {editor.kind === "delete" ? (
              <p>
                “
                {"name" in (row || {})
                  ? (row as Professor).name
                  : (row as Assignment).courseName}
                ” kaydı silinecek. Devam etmek istiyor musunuz?
              </p>
            ) : editor.kind === "photo" ? (
              <label>
                Fotoğraf (JPEG / PNG, en fazla 5 MB)
                <input
                  type="file"
                  name="file"
                  accept="image/jpeg,image/png"
                  required
                />
              </label>
            ) : (
              <>
                {editor.tab !== "assignments" ? (
                  <>
                    <label>
                      {editor.tab === "professors"
                        ? "Profesör adı"
                        : "Ders adı"}
                      <input
                        autoFocus
                        name="name"
                        required
                        maxLength={150}
                        pattern=".*\S.*"
                        defaultValue={(row as Professor)?.name}
                      />
                    </label>
                    {editor.tab === "professors" ? (
                      <>
                        <label>
                          Department
                          <input
                            name="department"
                            required
                            maxLength={150}
                            pattern=".*\S.*"
                            defaultValue={(row as Professor)?.department}
                          />
                        </label>
                        {row && (
                          <label className="checkbox">
                            <input type="checkbox" name="staged" /> Redis
                            üzerinden gecikmeli kaydet (15 sn)
                          </label>
                        )}
                      </>
                    ) : (
                      <label>
                        Credit
                        <input
                          type="number"
                          name="credit"
                          min={1}
                          max={30}
                          required
                          defaultValue={(row as Course)?.credit ?? 3}
                        />
                      </label>
                    )}
                  </>
                ) : (
                  <>
                    <label>
                      Professor
                      <select
                        name="professorId"
                        required
                        disabled={!!row}
                        defaultValue={(row as Assignment)?.professorId ?? ""}
                      >
                        <option value="">Profesör seçin</option>
                        {professors.map((p) => (
                          <option key={p.id} value={p.id}>
                            {p.name} · {p.department}
                          </option>
                        ))}
                      </select>
                    </label>
                    <label>
                      Course
                      <select
                        name="courseId"
                        required
                        disabled={!!row}
                        defaultValue={(row as Assignment)?.courseId ?? ""}
                      >
                        <option value="">Ders seçin</option>
                        {courses.map((c) => (
                          <option key={c.id} value={c.id}>
                            {c.name}
                          </option>
                        ))}
                      </select>
                    </label>
                    <label>
                      Start Date
                      <input
                        type="date"
                        name="startDate"
                        required
                        disabled={!!row}
                        defaultValue={(row as Assignment)?.startDate}
                      />
                    </label>
                    <label>
                      Student Count
                      <input
                        type="number"
                        name="studentCount"
                        min={0}
                        required
                        defaultValue={(row as Assignment)?.studentCount ?? 0}
                      />
                    </label>
                    <label>
                      Ending Date (isteğe bağlı)
                      <input
                        type="date"
                        name="endingDate"
                        min={(row as Assignment)?.startDate}
                        defaultValue={(row as Assignment)?.endingDate ?? ""}
                      />
                    </label>
                  </>
                )}
              </>
            )}
            {formError && (
              <p role="alert" className="error">
                {formError}
              </p>
            )}
            <div className="modal-actions">
              <button
                type="button"
                disabled={busy}
                onClick={() => setEditor(null)}
              >
                Vazgeç
              </button>
              <button
                className={editor.kind === "delete" ? "destructive" : "primary"}
                disabled={busy}
              >
                {busy
                  ? "İşleniyor…"
                  : editor.kind === "delete"
                    ? "Sil"
                    : "Kaydet"}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </main>
  );
}
