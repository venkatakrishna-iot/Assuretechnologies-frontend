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
  return response.data.data.services as BackendService[];
};
