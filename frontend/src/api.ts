export interface Professor {
  id: number;
  name: string;
  department: string;
  photoPath: string | null;
}
export interface Course {
  id: number;
  name: string;
  credit: number;
}
export interface Assignment {
  professorId: number;
  professorName: string;
  department: string;
  photoPath: string | null;
  courseId: number;
  courseName: string;
  credit: number;
  studentCount: number;
  startDate: string;
  endingDate: string | null;
}
export async function api<T>(
  path: string,
  method = "GET",
  body?: unknown,
): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`/api${path}`, {
      method,
      headers:
        body instanceof FormData ? {} : { "Content-Type": "application/json" },
      body:
        body === undefined
          ? undefined
          : body instanceof FormData
            ? body
            : JSON.stringify(body),
    });
  } catch {
    throw new Error("Sunucuya ulaşılamadı. Backend bağlantısını kontrol edin.");
  }
  const text = await response.text();
  let data;
  try {
    data = text ? JSON.parse(text) : undefined;
  } catch {
    data = text;
  }
  if (!response.ok)
    throw new Error(
      data?.detail ||
        data?.message ||
        (response.status === 409
          ? "Bu kayıt kullanımda veya aynı öğretim ataması zaten mevcut. Önce ilişkili atamaları kontrol edin."
          : `İşlem tamamlanamadı (${response.status}).`),
    );
  return data as T;
}
export const assignmentPath = (a: Assignment) =>
  `/assignments/${a.professorId}/${a.courseId}/${a.startDate}`;
