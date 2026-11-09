import axiosInstance from './axiosConfig';

export interface CustomField {
  id: string;
  type: string;
  label: string;
  required: boolean;
  options?: string[];
}

export interface PricingType {
  id: string;
  name: string;
  label: string;
}

export interface BackendService {
  id: string;
  name: string;
  image?: string;
  prebooking_charge?: string;
  price?: string;
  service_owner_type?: 'ADMIN' | 'PARTNER';
  pricing_type?: PricingType;
  custom_fields?: CustomField[];
  category?: {
    id: string;
    name: string;
  };
  rate?: number;
  pricingUnitName?: string;
  pricingUnitLabel?: string;
  tax_rate?: number;
  admin_commission_rate?: number;
}

export const fetchAllServices = async (search = '') => {
  const response = await axiosInstance.get(`/services`, { params: { search } });
  const raw = response.data?.data;
  const list = Array.isArray(raw) ? raw : (Array.isArray(raw?.services) ? raw.services : []);
  
  return list.map((item: any) => ({
    ...item,
    category: typeof item.category === 'string'
      ? { id: item.category, name: item.category }
      : item.category,
    pricing_type: typeof item.pricing_type === 'string'
      ? { id: item.pricing_type, name: item.pricing_type, label: item.pricing_type }
      : item.pricing_type
  })) as BackendService[];
};
