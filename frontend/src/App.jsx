import React, { useEffect, useMemo, useState } from "react";

const statusOptions = [
  "AVAILABLE",
  "RESERVED",
  "OCCUPIED",
  "CLEANING",
  "MAINTENANCE"
];

const emptyRoom = {
  number: "",
  type: "",
  status: "AVAILABLE",
  floor: 1,
  pricePerNight: 0,
  occupantName: "",
  notes: ""
};

const defaultAuth = {
  username: "staff",
  password: "staff123"
};

export function App() {
  const [auth, setAuth] = useState(defaultAuth);
  const [activeUser, setActiveUser] = useState("");
  const [rooms, setRooms] = useState([]);
  const [stats, setStats] = useState(null);
  const [audit, setAudit] = useState([]);
  const [filters, setFilters] = useState({
    q: "",
    status: "",
    floor: "",
    sort: "number"
  });
  const [roomForm, setRoomForm] = useState(emptyRoom);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const isAdmin = activeUser === "admin";

  const authHeaders = useMemo(() => {
    const token = btoa(`${auth.username}:${auth.password}`);
    return { Authorization: `Basic ${token}` };
  }, [auth.username, auth.password]);

  const loadDashboard = async () => {
    setLoading(true);
    setError("");
    setMessage("");
    try {
      const query = new URLSearchParams();
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== "") {
          query.set(key, value);
        }
      });

      const [roomsRes, statsRes] = await Promise.all([
        fetch(`/api/rooms?${query.toString()}`, { headers: authHeaders }),
        fetch("/api/rooms/stats", { headers: authHeaders })
      ]);

      if (!roomsRes.ok) {
        throw new Error(`Rooms request failed with ${roomsRes.status}`);
      }
      if (!statsRes.ok) {
        throw new Error(`Stats request failed with ${statsRes.status}`);
      }

      setRooms(await roomsRes.json());
      setStats(await statsRes.json());
      const signedInUser = auth.username.trim().toLowerCase();
      setActiveUser(signedInUser);

      if (signedInUser === "admin") {
        const auditRes = await fetch("/api/admin/audit?limit=12", {
          headers: authHeaders
        });
        setAudit(auditRes.ok ? await auditRes.json() : []);
      } else {
        setAudit([]);
      }
    } catch (e) {
      setRooms([]);
      setStats(null);
      setAudit([]);
      setActiveUser("");
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const saveRoom = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    try {
      const res = await fetch("/api/admin/rooms", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders
        },
        body: JSON.stringify({
          ...roomForm,
          floor: Number(roomForm.floor),
          pricePerNight: Number(roomForm.pricePerNight)
        })
      });
      if (!res.ok) {
        throw new Error(`Save failed with ${res.status}`);
      }
      setRoomForm(emptyRoom);
      setMessage("Room inventory saved.");
      await loadDashboard();
    } catch (e) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  };

  const changeStatus = async (room, status) => {
    setError("");
    setMessage("");
    try {
      const res = await fetch(`/api/admin/rooms/${encodeURIComponent(room.number)}/status`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders
        },
        body: JSON.stringify({
          status,
          occupantName: room.occupantName,
          notes: room.notes
        })
      });
      if (!res.ok) {
        throw new Error(`Status change failed with ${res.status}`);
      }
      setMessage(`Room ${room.number} moved to ${status}.`);
      await loadDashboard();
    } catch (e) {
      setError(e.message);
    }
  };

  const deleteRoom = async (number) => {
    const confirmed = window.confirm(`Delete room ${number}?`);
    if (!confirmed) {
      return;
    }
    setError("");
    setMessage("");
    try {
      const res = await fetch(`/api/admin/rooms/${encodeURIComponent(number)}`, {
        method: "DELETE",
        headers: authHeaders
      });
      if (!res.ok && res.status !== 404) {
        throw new Error(`Delete failed with ${res.status}`);
      }
      setMessage(`Room ${number} deleted.`);
      await loadDashboard();
    } catch (e) {
      setError(e.message);
    }
  };

  const editRoom = (room) => {
    setRoomForm({
      number: room.number,
      type: room.type,
      status: room.status,
      floor: room.floor,
      pricePerNight: room.pricePerNight,
      occupantName: room.occupantName || "",
      notes: room.notes || ""
    });
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Hotel Security Operations</p>
          <h1>Room Command Center</h1>
        </div>
        <div className="session-card">
          <label>
            User
            <input
              value={auth.username}
              onChange={(event) => setAuth({ ...auth, username: event.target.value })}
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={auth.password}
              onChange={(event) => setAuth({ ...auth, password: event.target.value })}
            />
          </label>
          <button onClick={loadDashboard} disabled={loading}>
            {loading ? "Loading" : "Sign in"}
          </button>
        </div>
      </header>

      {(error || message) && (
        <div className={error ? "notice notice-error" : "notice notice-success"}>
          {error || message}
        </div>
      )}

      <section className="stats-grid">
        <Metric label="Total rooms" value={stats?.totalRooms ?? "-"} />
        <Metric label="Available" value={stats?.availableRooms ?? "-"} />
        <Metric label="Occupied" value={stats?.occupiedRooms ?? "-"} />
        <Metric
          label="Nightly revenue"
          value={stats ? `Rs. ${Number(stats.projectedNightlyRevenue).toLocaleString("en-IN")}` : "-"}
        />
      </section>

      <main className="workspace">
        <section className="panel panel-wide">
          <div className="panel-title">
            <div>
              <h2>Room Inventory</h2>
              <p>{activeUser ? `${activeUser} session` : "Not signed in"}</p>
            </div>
            <button onClick={loadDashboard} disabled={loading}>
              Refresh
            </button>
          </div>

          <div className="filters">
            <input
              placeholder="Search room, guest, type, note"
              value={filters.q}
              onChange={(event) => setFilters({ ...filters, q: event.target.value })}
            />
            <select
              value={filters.status}
              onChange={(event) => setFilters({ ...filters, status: event.target.value })}
            >
              <option value="">All statuses</option>
              {statusOptions.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
            <input
              min="1"
              placeholder="Floor"
              type="number"
              value={filters.floor}
              onChange={(event) => setFilters({ ...filters, floor: event.target.value })}
            />
            <select
              value={filters.sort}
              onChange={(event) => setFilters({ ...filters, sort: event.target.value })}
            >
              <option value="number">Sort by room</option>
              <option value="floor">Sort by floor</option>
              <option value="status">Sort by status</option>
              <option value="price">Sort by price</option>
              <option value="updated">Sort by updated</option>
            </select>
          </div>

          <div className="room-table" role="table" aria-label="Room inventory">
            <div className="table-row table-head" role="row">
              <span>Room</span>
              <span>Status</span>
              <span>Guest</span>
              <span>Rate</span>
              <span>Actions</span>
            </div>
            {rooms.map((room) => (
              <div className="table-row" role="row" key={room.number}>
                <span>
                  <strong>{room.number}</strong>
                  <small>{room.type} · Floor {room.floor}</small>
                </span>
                <span className={`status-pill status-${room.status.toLowerCase()}`}>
                  {room.status}
                </span>
                <span>{room.occupantName || "Unassigned"}</span>
                <span>Rs. {Number(room.pricePerNight).toLocaleString("en-IN")}</span>
                <span className="row-actions">
                  {isAdmin && (
                    <>
                      <select
                        value={room.status}
                        onChange={(event) => changeStatus(room, event.target.value)}
                        aria-label={`Change status for room ${room.number}`}
                      >
                        {statusOptions.map((status) => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                      <button className="secondary" onClick={() => editRoom(room)}>
                        Edit
                      </button>
                      <button className="danger" onClick={() => deleteRoom(room.number)}>
                        Delete
                      </button>
                    </>
                  )}
                </span>
              </div>
            ))}
          </div>
        </section>

        {isAdmin && (
          <aside className="side-stack">
            <section className="panel">
              <h2>Room Editor</h2>
              <form className="room-form" onSubmit={saveRoom}>
                <label>
                  Room
                  <input
                    required
                    value={roomForm.number}
                    onChange={(event) => setRoomForm({ ...roomForm, number: event.target.value })}
                  />
                </label>
                <label>
                  Type
                  <input
                    required
                    value={roomForm.type}
                    onChange={(event) => setRoomForm({ ...roomForm, type: event.target.value })}
                  />
                </label>
                <div className="form-grid">
                  <label>
                    Floor
                    <input
                      min="1"
                      required
                      type="number"
                      value={roomForm.floor}
                      onChange={(event) => setRoomForm({ ...roomForm, floor: event.target.value })}
                    />
                  </label>
                  <label>
                    Rate
                    <input
                      min="0"
                      required
                      type="number"
                      value={roomForm.pricePerNight}
                      onChange={(event) =>
                        setRoomForm({ ...roomForm, pricePerNight: event.target.value })
                      }
                    />
                  </label>
                </div>
                <label>
                  Status
                  <select
                    value={roomForm.status}
                    onChange={(event) => setRoomForm({ ...roomForm, status: event.target.value })}
                  >
                    {statusOptions.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Guest
                  <input
                    value={roomForm.occupantName}
                    onChange={(event) =>
                      setRoomForm({ ...roomForm, occupantName: event.target.value })
                    }
                  />
                </label>
                <label>
                  Notes
                  <textarea
                    value={roomForm.notes}
                    onChange={(event) => setRoomForm({ ...roomForm, notes: event.target.value })}
                  />
                </label>
                <button disabled={saving}>{saving ? "Saving" : "Save room"}</button>
              </form>
            </section>

            <section className="panel">
              <h2>Audit Trail</h2>
              <div className="audit-list">
                {audit.length === 0 && <p className="muted">No audit events yet.</p>}
                {audit.map((event) => (
                  <article key={`${event.timestamp}-${event.action}-${event.target}`}>
                    <strong>{event.action}</strong>
                    <span>{event.target} · {event.actor}</span>
                    <small>{new Date(event.timestamp).toLocaleString()}</small>
                    <p>{event.details}</p>
                  </article>
                ))}
              </div>
            </section>
          </aside>
        )}
      </main>
    </div>
  );
}

function Metric({ label, value }) {
  return (
    <article className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}
