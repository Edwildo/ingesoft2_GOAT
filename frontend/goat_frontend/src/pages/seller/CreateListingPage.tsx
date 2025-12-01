import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { listingService } from "../../api/listing.service";
import { catalogService } from "../../api/catalog.service";
import { Card } from "../../components/common/Card";
import { Button } from "../../components/common/Button";
import { Alert } from "../../components/common/Alert";
import { Input } from "../../components/common/Input";
import { SkuAutocomplete } from "../../components/seller/SkuAutocomplete";
import { CreateSneakerModal } from "../../components/seller/CreateSneakerModal";
import {
  CreateListingRequest,
  Condition,
  Gender,
} from "../../types/listing.types";
import { Sneaker, CreateSneakerRequest } from "../../types/catalog.types";
import styles from "./CreateListingPage.module.css";

export const CreateListingPage: React.FC = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<CreateListingRequest>({
    sneakerSku: "",
    size: "",
    condition: "NEW",
    gender: "UNISEX",
    brand: "",
    color: "",
    price: 0,
    coverImage: "",
  });

  const [selectedSneaker, setSelectedSneaker] = useState<Sneaker | null>(null);
  const [errors, setErrors] = useState<
    Partial<Record<keyof CreateListingRequest, string>>
  >({});
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);
  const [showCreateSneakerModal, setShowCreateSneakerModal] = useState(false);
  const [pendingSku, setPendingSku] = useState<string>("");

  const handleSkuSelect = (sneaker: Sneaker) => {
    setSelectedSneaker(sneaker);
    setFormData((prev) => ({
      ...prev,
      brand: sneaker.brand,
      gender: sneaker.gender as Gender,
      coverImage: sneaker.media?.coverImage || prev.coverImage,
    }));
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof CreateListingRequest, string>> = {};

    if (!formData.sneakerSku) {
      newErrors.sneakerSku = "El SKU es requerido";
    }

    if (!formData.size) {
      newErrors.size = "La talla es requerida";
    }

    if (!formData.brand) {
      newErrors.brand = "La marca es requerida";
    }

    if (!formData.color) {
      newErrors.color = "El color es requerido";
    }

    if (formData.price <= 0) {
      newErrors.price = "El precio debe ser mayor a 0";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    setMessage(null);

    try {
      // 1. Verificar si el SKU existe en el catálogo
      const sneakerCheck = await catalogService.getSneakerBySku(
        formData.sneakerSku
      );

      if (!sneakerCheck.success) {
        // Si el error es 404, el SKU no existe
        if (sneakerCheck.error?.status === 404) {
          setPendingSku(formData.sneakerSku);
          setShowCreateSneakerModal(true);
          setIsLoading(false);
          return;
        } else {
          // Otro error
          setMessage({
            type: "error",
            text: sneakerCheck.error?.message || "Error al verificar el SKU",
          });
          setIsLoading(false);
          return;
        }
      }

      // 2. SKU existe, proceder a crear el listing
      const response = await listingService.createListing(formData);

      if (response.success && response.data) {
        setMessage({ type: "success", text: "Listing creado exitosamente" });
        setTimeout(() => {
          navigate("/seller/listings");
        }, 1500);
      } else {
        setMessage({
          type: "error",
          text: response.error?.message || "Error al crear listing",
        });
      }
    } catch (err) {
      console.error("Create listing error:", err);
      setMessage({ type: "error", text: "Error de conexión con el servidor" });
    } finally {
      setIsLoading(false);
    }
  };

  const handleSneakerCreated = async (sneakerData: CreateSneakerRequest) => {
    // Actualizar el formulario con los datos del sneaker creado
    setFormData((prev) => ({
      ...prev,
      brand: sneakerData.brand,
      gender: sneakerData.gender as Gender,
      coverImage: sneakerData.media?.coverImage || prev.coverImage,
    }));

    // Crear el listing ahora que el SKU existe
    setIsLoading(true);
    setMessage(null);

    try {
      const response = await listingService.createListing(formData);

      if (response.success && response.data) {
        setMessage({
          type: "success",
          text: "Sneaker y listing creados exitosamente",
        });
        setTimeout(() => {
          navigate("/seller/listings");
        }, 1500);
      } else {
        setMessage({
          type: "error",
          text: response.error?.message || "Error al crear listing",
        });
      }
    } catch (err) {
      console.error("Create listing after sneaker creation error:", err);
      setMessage({ type: "error", text: "Error de conexión con el servidor" });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.createListingPage}>
      <div className={styles.container}>
        <Card className={styles.card}>
          <h1 className={styles.title}>Crear Nuevo Listing</h1>

          {message && (
            <Alert
              variant={message.type === "error" ? "error" : "success"}
              onClose={() => setMessage(null)}
            >
              {message.text}
            </Alert>
          )}

          <form onSubmit={handleSubmit} className={styles.form}>
            <SkuAutocomplete
              value={formData.sneakerSku}
              onChange={(sku) =>
                setFormData((prev) => ({ ...prev, sneakerSku: sku }))
              }
              onSneakerSelect={handleSkuSelect}
              error={errors.sneakerSku}
              disabled={isLoading}
            />

            <div className={styles.row}>
              <Input
                id="brand"
                label="Marca"
                value={formData.brand}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, brand: e.target.value }))
                }
                error={errors.brand}
                required
                fullWidth
                disabled={!!selectedSneaker}
              />

              <Input
                id="size"
                label="Talla"
                value={formData.size}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, size: e.target.value }))
                }
                error={errors.size}
                required
                fullWidth
              />
            </div>

            <div className={styles.row}>
              <div className={styles.selectGroup}>
                <label htmlFor="condition" className={styles.label}>
                  Condición <span className={styles.required}>*</span>
                </label>
                <select
                  id="condition"
                  value={formData.condition}
                  onChange={(e) =>
                    setFormData((prev) => ({
                      ...prev,
                      condition: e.target.value as Condition,
                    }))
                  }
                  className={styles.select}
                  required
                >
                  <option value="NEW">Nuevo</option>
                  <option value="LIKE_NEW">Como nuevo</option>
                  <option value="USED">Usado</option>
                  <option value="FAIR">Regular</option>
                </select>
              </div>

              <div className={styles.selectGroup}>
                <label htmlFor="gender" className={styles.label}>
                  Género <span className={styles.required}>*</span>
                </label>
                <select
                  id="gender"
                  value={formData.gender}
                  onChange={(e) =>
                    setFormData((prev) => ({
                      ...prev,
                      gender: e.target.value as Gender,
                    }))
                  }
                  className={styles.select}
                  required
                  disabled={!!selectedSneaker}
                >
                  <option value="MALE">Masculino</option>
                  <option value="FEMALE">Femenino</option>
                  <option value="UNISEX">Unisex</option>
                </select>
              </div>
            </div>

            <Input
              id="color"
              label="Color"
              value={formData.color}
              onChange={(e) =>
                setFormData((prev) => ({ ...prev, color: e.target.value }))
              }
              error={errors.color}
              required
              fullWidth
            />

            <Input
              id="price"
              label="Precio (COP)"
              type="number"
              min="0"
              step="0.01"
              value={formData.price || ""}
              onChange={(e) =>
                setFormData((prev) => ({
                  ...prev,
                  price: parseFloat(e.target.value) || 0,
                }))
              }
              error={errors.price}
              required
              fullWidth
            />

            <Input
              id="coverImage"
              label="URL de Imagen"
              type="url"
              value={formData.coverImage}
              onChange={(e) =>
                setFormData((prev) => ({ ...prev, coverImage: e.target.value }))
              }
              fullWidth
              helperText="URL de la imagen principal del sneaker"
            />

            <div className={styles.actions}>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate("/seller/listings")}
                disabled={isLoading}
              >
                Cancelar
              </Button>
              <Button type="submit" variant="primary" isLoading={isLoading}>
                Crear Listing
              </Button>
            </div>
          </form>
        </Card>
      </div>

      {showCreateSneakerModal && (
        <CreateSneakerModal
          sku={pendingSku}
          onClose={() => setShowCreateSneakerModal(false)}
          onSuccess={handleSneakerCreated}
        />
      )}
    </div>
  );
};
