import React, { useState } from "react";
import axios from "axios";

export default function RunPayrollButton() {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(null);

  const handleRunPayroll = async () => {
    setLoading(true);
    setSuccess(null);
    try {
      // You may need to add JWT auth headers here
      await axios.post("/api/admin/debug/run-payroll");
      setSuccess("Payroll automation completed successfully!");
    } catch (err) {
      setSuccess("Error running payroll automation.");
    }
    setLoading(false);
  };

  return (
    <div>
      <button
        className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
        onClick={handleRunPayroll}
        disabled={loading}
      >
        {loading ? "Running..." : "Run Monthly Automation"}
      </button>
      {success && (
        <div className={`mt-2 ${success.startsWith("Error") ? "text-red-600" : "text-green-600"}`}>
          {success}
        </div>
      )}
    </div>
  );
}
