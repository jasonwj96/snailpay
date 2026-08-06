// lib/upload.ts
export async function uploadFile(file: File, customerId: string) {

  // Step 1 — get presigned URL from your API
  const presignRes = await fetch("/api/upload/presign", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      filename:     file.name,
      content_type: file.type,
      customer_id:  customerId,
    }),
  });

  const { url, key } = await presignRes.json();

  // Step 2 — upload directly to S3
  await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": file.type },
    body: file,
  });

  // Step 3 — confirm with your API to save to postgres
  const confirmRes = await fetch("/api/upload/confirm", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ key, customer_id: customerId }),
  });

  return confirmRes.json();
}