import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { listingService } from "../../api/listing.service";
import { catalogService } from "../../api/catalog.service";
import { Card } from "../../components/common/Card";
import { Button } from "../../components/common/Button";
import { Alert } from "../../components/common/Alert";
import { Input } from "../../components/common/Input";
import { SkuAutocomplete } from "../../components/seller/SkuAutocomplete";
import {
  CreateListingRequest,
  Condition,
  Gender,
} from "../../types/listing.types";
import { Sneaker } from "../../types/catalog.types";
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
  const [sneakerExists, setSneakerExists] = useState<boolean>(true);
  const [sneakerCreated, setSneakerCreated] = useState<boolean>(false);
  const [sneakerModel, setSneakerModel] = useState<string>("");
  const [sneakerDescription, setSneakerDescription] = useState<string>("");
  const [errors, setErrors] = useState<
    Partial<Record<keyof CreateListingRequest | "sneakerModel", string>>
  >({});
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);

  const handleSkuSelect = (sneaker: Sneaker) => {
    setSelectedSneaker(sneaker);
    setSneakerExists(true);
    setFormData((prev) => ({
      ...prev,
      brand: sneaker.brand,
      gender: sneaker.gender as Gender,
      coverImage: sneaker.media?.coverImage || prev.coverImage,
    }));
  };

  const handleSkuChange = async (sku: string) => {
    setFormData((prev) => ({ ...prev, sneakerSku: sku }));
    setSelectedSneaker(null);
    setSneakerModel("");
    setSneakerDescription("");
    setSneakerCreated(false);

    // Verificar si el SKU existe cuando el usuario lo escribe
    if (sku.trim().length >= 3) {
      try {
        const sneakerCheck = await catalogService.getSneakerBySku(sku.trim());
        setSneakerExists(sneakerCheck.success);
        
        // Si existe, prellenar datos
        if (sneakerCheck.success && sneakerCheck.data) {
          setFormData((prev) => ({
            ...prev,
            brand: sneakerCheck.data!.brand,
            gender: sneakerCheck.data!.gender as Gender,
            coverImage: sneakerCheck.data!.media?.coverImage || prev.coverImage,
          }));
        }
      } catch (err) {
        // Silencioso, se verificará al hacer submit
        setSneakerExists(true);
      }
    } else {
      setSneakerExists(true);
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof CreateListingRequest | "sneakerModel", string>> = {};

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

    // Si el sneaker no existe, el modelo es requerido
    if (!sneakerExists && !sneakerModel.trim()) {
      newErrors.sneakerModel = "El modelo es requerido para crear el sneaker";
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
        // Si el error es 404, el SKU no existe - crear sneaker automáticamente
        if (sneakerCheck.error?.status === 404) {
          setSneakerExists(false);
          
          // Validar que tenemos los datos necesarios para crear el sneaker
          if (!sneakerModel.trim()) {
            setMessage({
              type: "error",
              text: "El modelo es requerido para crear el sneaker",
            });
            setIsLoading(false);
            return;
          }

          // Crear el sneaker automáticamente
          const createSneakerResponse = await catalogService.createSneaker({
            sku: formData.sneakerSku.toUpperCase(),
            brand: formData.brand,
            model: sneakerModel.trim(),
            gender: formData.gender,
            description: sneakerDescription.trim() || undefined,
            categories: [],
            collections: [],
            media: formData.coverImage
              ? {
                  coverImage: formData.coverImage,
                  gallery: [],
                }
              : undefined,
          });

          if (!createSneakerResponse.success) {
            setMessage({
              type: "error",
              text:
                createSneakerResponse.error?.message ||
                "Error al crear el sneaker en el catálogo",
            });
            setIsLoading(false);
            return;
          }

          setSneakerCreated(true);
          setSneakerExists(true);

          // Esperar un momento para que el sneaker se propague
          await new Promise((resolve) => setTimeout(resolve, 500));
        } else {
          // Otro error
          setMessage({
            type: "error",
            text: sneakerCheck.error?.message || "Error al verificar el SKU",
          });
          setIsLoading(false);
          return;
        }
      } else {
        setSneakerExists(true);
      }

      // 2. Crear el listing (el SKU ahora existe)
      const response = await listingService.createListing(formData);

      if (response.success && response.data) {
        setMessage({
          type: "success",
          text: sneakerCreated
            ? "Sneaker y listing creados exitosamente"
            : "Listing creado exitosamente",
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
      console.error("Create listing error:", err);
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
              onChange={handleSkuChange}
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

            {/* Campos para crear sneaker si no existe */}
            {!selectedSneaker && (
              <div className={styles.row}>
                <Input
                  id="sneakerModel"
                  label="Modelo del Sneaker"
                  value={sneakerModel}
                  onChange={(e) => setSneakerModel(e.target.value)}
                  error={errors.sneakerModel}
                  required={!sneakerExists}
                  fullWidth
                  helperText={
                    sneakerExists
                      ? "Opcional (el sneaker ya existe)"
                      : "Requerido para crear el sneaker en el catálogo"
                  }
                />

                <Input
                  id="sneakerDescription"
                  label="Descripción del Sneaker"
                  value={sneakerDescription}
                  onChange={(e) => setSneakerDescription(e.target.value)}
                  fullWidth
                  helperText="Opcional"
                />
              </div>
            )}

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
                {sneakerExists || selectedSneaker
                  ? "Crear Listing"
                  : "Crear Sneaker y Listing"}
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
};
