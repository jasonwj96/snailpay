"use client";

import { useState } from "react";
import { uploadFile } from "@/app/lib//upload";

export default function FileUpload({ customerId }: { customerId: string }) {
  const [uploading, setUploading] = useState(false);
  const [done, setDone] = useState(false);

  async function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    await uploadFile(file, customerId);
    setUploading(false);
    setDone(true);
  }

  return (
    <div>
      <input type="file" onChange={handleChange} disabled={uploading} />
      {uploading && <p>Uploading...</p>}
      {done && <p>Upload complete</p>}
    </div>
  );
}