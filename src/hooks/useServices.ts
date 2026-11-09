import { useState, useEffect } from 'react';
import { fetchAllServices, type BackendService } from '../api/servicesApi';

export function useServices() {
  const [services, setServices] = useState<BackendService[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAllServices()
      .then(data => setServices(Array.isArray(data) ? data : []))
      .catch(err => {
        console.error('Failed to load services', err);
        setServices([]);
      })
      .finally(() => setLoading(false));
  }, []);

  return { services, loading };
}
